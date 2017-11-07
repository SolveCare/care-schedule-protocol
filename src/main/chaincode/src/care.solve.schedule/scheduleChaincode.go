package main

import (
	"crypto/x509"
	"encoding/pem"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/golang/protobuf/proto"

	mspprotos "github.com/hyperledger/fabric/protos/msp"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("schedule_chaincode")

type ScheduleChaincode struct {
	scheduler SchedulerImpl
	doctorService *DoctorService
	patientService *PatientService

	scheduleService *ScheduleService

	dispatcher Dispatcher
}

func (s *ScheduleChaincode) Init(stub shim.ChaincodeStubInterface) pb.Response {
	s.scheduler = SchedulerImpl{}
	logger.Infof("Created Scheduler: %v", s.scheduler)

	s.doctorService = &DoctorService{}
	logger.Infof("Created DoctorService: %v", s.doctorService)

	s.patientService = &PatientService{}
	logger.Infof("Created PatientService: %v", s.patientService)

	s.scheduleService = new(ScheduleService).New(&s.scheduler, s.doctorService, s.patientService)
	logger.Infof("Created ScheduleService: %v", s.scheduleService)

	s.dispatcher = Dispatcher{}

	s.dispatcher.AddMapping(PatientFunctions_PATIENT_GET_BY_ID.String(), s.getPatientById)
	s.dispatcher.AddMapping(PatientFunctions_PATIENT_CREATE.String(), s.createPatient)

	s.dispatcher.AddMapping(DoctorFunctions_DOCTOR_CREATE.String(), s.createDoctor)
	s.dispatcher.AddMapping(DoctorFunctions_DOCTOR_GET_BY_ID.String(), s.getDoctorById)
	s.dispatcher.AddMapping(DoctorFunctions_DOCTOR_GET_ALL.String(), s.getAllDoctors)

	s.dispatcher.AddMapping(ScheduleFunctions_SCHEDULE_GET_BY_OWNER_ID.String(), s.getScheduleByOwnerId)
	s.dispatcher.AddMapping(ScheduleFunctions_SCHEDULE_CREATE.String(), s.createSchedule)

	s.dispatcher.AddMapping(SlotFunctions_SLOT_CREATE.String(), s.createSlot)
	s.dispatcher.AddMapping(SlotFunctions_SLOT_UPDATE.String(), s.updateSlot)

	return shim.Success(nil)
}

func (s *ScheduleChaincode) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	function, args := stub.GetFunctionAndParameters()

	logger.Infof("=====================================================")
	logger.Infof("Invoking function %v with args %v", function, args)

	printSigner(stub)

	return s.dispatcher.Dispatch(stub)
}

func (s *ScheduleChaincode) createPatient(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	encodedPatientByteString := args[0]
	patient, err := s.patientService.decodeProtoByteString(encodedPatientByteString)
	if err != nil {
		//return shim.Error(err.Error()) //todo: investigate 'proto: bad wiretype for field main.Patient.UserId: got wiretype 1, want 2'
	}
	savedPatient, err := s.patientService.savePatient(stub, *patient)
	if err != nil {
		return shim.Error(err.Error())
	}
	return s.getResponseWithProto(savedPatient)
}

func (s *ScheduleChaincode) createDoctor(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	encodedDoctorByteString := args[0]
	doctor, err := s.doctorService.decodeProtoByteString(encodedDoctorByteString)
	if err != nil {
		return shim.Error(err.Error())
	}
	savedDoctor, err := s.doctorService.saveDoctor(stub, *doctor)
	if err != nil {
		return shim.Error(err.Error())
	}
	return s.getResponseWithProto(savedDoctor)
}

func (s *ScheduleChaincode) getDoctorById(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	doctorId := args[0]
	doctor, err := s.doctorService.getDoctorById(stub, doctorId)
	if err != nil {
		return shim.Error(err.Error())
	}
	return s.getResponseWithProto(doctor)
}

func (s *ScheduleChaincode) getAllDoctors(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	doctors, err := s.doctorService.getAllDoctors(stub)
	if err != nil {
		return shim.Error(err.Error())
	}

	doctorCollection := DoctorCollection{doctors}

	return s.getResponseWithProto(&doctorCollection)
}

func (s *ScheduleChaincode) getPatientById(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	patientId := args[0]
	patient, err := s.patientService.getPatientById(stub, patientId)
	if err != nil {
		return shim.Error(err.Error())
	}
	return s.getResponseWithProto(patient)
}

func (s *ScheduleChaincode) getScheduleByOwnerId(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	ownerId := args[0]
	schedule, err := s.scheduleService.getScheduleByOwnerId(stub, ownerId)
	if err != nil {
		return shim.Error(err.Error())
	}
	return s.getResponseWithProto(schedule)
}

func (s *ScheduleChaincode) createSchedule(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	scheduleByteString := args[0]
	schedule, err := s.scheduleService.decodeScheduleByteString(scheduleByteString)
	if err != nil {
		return shim.Error(err.Error())
	}
	createdSchedule, err := s.scheduleService.createSchedule(stub, *schedule)
	return s.getResponseWithProto(createdSchedule)
}

func (s *ScheduleChaincode) createSlot(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	scheduleId := args[0]
	slotByteString := args[1]
	slot, err := s.scheduleService.decodeSlotByteString(slotByteString)
	if err != nil {
		return shim.Error(err.Error())
	}
	createdSlot, err := s.scheduleService.createSlot(stub, scheduleId, *slot)
	return s.getResponseWithProto(createdSlot)
}

func (s *ScheduleChaincode) updateSlot(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	scheduleId := args[0]
	slotId := args[1]
	slotByteString := args[2]
	slot, err := s.scheduleService.decodeSlotByteString(slotByteString)
	if err != nil {
		return shim.Error(err.Error())
	}
	err = s.scheduleService.updateSlot(stub, scheduleId, slotId, *slot)
	return shim.Success(nil)
}

func (s *ScheduleChaincode) getResponseWithProto(message proto.Message) pb.Response {
	doctorBytes, err := proto.Marshal(message)
	if err != nil {
		logger.Errorf("Failed to marshall message: '%v'. Error: %v", message, err)
		return shim.Error(err.Error())
	}

	return shim.Success(doctorBytes)
}

func printSigner(stub shim.ChaincodeStubInterface) {
	logger.Infof("*********************************")
	creator,err := stub.GetCreator()
	if err != nil {
		logger.Errorf("Error: %v", err.Error())
	}

	id := &mspprotos.SerializedIdentity{}
	err = proto.Unmarshal(creator, id)

	logger.Infof("Creator: %v", string(creator))

	block, _ := pem.Decode(id.GetIdBytes())
	cert,err := x509.ParseCertificate(block.Bytes)

	enrollID := cert.Subject.CommonName
	logger.Infof("enrollID: %v", string(enrollID))

	mspID := id.GetMspid()
	logger.Infof("mspID: %v \n", string(mspID))
	logger.Infof("*********************************")
}

func main() {
	err := shim.Start(new(ScheduleChaincode))
	if err != nil {
		logger.Errorf("Error starting ScheduleChaincode: %s", err)
	}
}
