package impl

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/golang/protobuf/proto"
	"encoding/json"

	careproto "../proto"
	careprotocol "../../protocol"

)

type PatientServiceDefault struct {
	logger *shim.ChaincodeLogger
}

func NewPatientService() careprotocol.PatientService {
	var logger = shim.NewLogger("patient_service")
	return &PatientServiceDefault{logger}
}


func (t *PatientServiceDefault) DecodeProtoByteString(encodedPatientByteString string) (*careproto.Patient, error) {
	var err error

	patient := careproto.Patient{}
	err = proto.Unmarshal([]byte(encodedPatientByteString), &patient)
	if err != nil {
		t.logger.Errorf("Error while unmarshalling Patient: %v", err.Error())
	}

	return &patient, err
}

func (t *PatientServiceDefault) SavePatient(stub shim.ChaincodeStubInterface, patient careproto.Patient) (*careproto.Patient, error) {
	t.logger.Infof("Saving patient %v", patient)

	jsonUser, err := json.Marshal(&patient)

	patientKey := "patient:" + patient.PatientId

	err = stub.PutState(patientKey, jsonUser)
	if err != nil {
		t.logger.Errorf("Error while saving patient '%v'. Error: %v", patient, err)
		return nil, err
	}

	return &patient, nil
}

func (t *PatientServiceDefault) GetPatientById(stub shim.ChaincodeStubInterface, patientId string) (*careproto.Patient, error) {

	patientKey := "patient:" + patientId
	patientBytes, err := stub.GetState(patientKey)
	if err != nil {
		t.logger.Errorf("Error while getting patient with key '%v'. Error: %v", patientKey, err)
		return nil, err
	}

	t.logger.Infof("Getting patient %v \n", string(patientBytes))

	var patient careproto.Patient
	json.Unmarshal(patientBytes, &patient)
	return &patient, nil
}