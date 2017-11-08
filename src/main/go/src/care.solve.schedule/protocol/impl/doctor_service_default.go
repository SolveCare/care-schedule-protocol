package impl

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"errors"
	"github.com/golang/protobuf/proto"
	"fmt"
	"encoding/json"

	careproto "../proto"
	careprotocol "../../protocol"
)

type DoctorServiceDefault struct {
	logger *shim.ChaincodeLogger
}

func NewDoctorService() careprotocol.DoctorService {
	var logger = shim.NewLogger("doctor_service")
	return &DoctorServiceDefault{logger}
}

func (s *DoctorServiceDefault) GetAllDoctors(stub shim.ChaincodeStubInterface) ([]*careproto.Doctor, error) {
	query := `{
		"selector":{
			"DoctorId":{"$regex":""}
		}
	}
	`

	resultsIterator, err := stub.GetQueryResult(query)
	if err != nil {
		return nil, err
	}
	defer resultsIterator.Close()

	s.logger.Infof("resultsIterator: %v", resultsIterator)

	doctors := make([]*careproto.Doctor, 0)
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		s.logger.Infof("queryResponse: %v", queryResponse)
		if err != nil {
			return nil, err
		}

		var doctor careproto.Doctor
		json.Unmarshal(queryResponse.Value, &doctor)
		s.logger.Infof("doctor: %v", doctor)
		doctors = append(doctors, &doctor)
	}

	return doctors, nil
}

func (s *DoctorServiceDefault) GetDoctorById(stub shim.ChaincodeStubInterface, doctorId string) (*careproto.Doctor, error) {
	doctorKey := "doctor:" + doctorId
	doctorBytes, err := stub.GetState(doctorKey)
	if err != nil {
		return nil, err
	}
	if doctorBytes == nil {
		errorMsg := fmt.Sprintf("Doctor with key '%v' not found", doctorKey)
		s.logger.Errorf(errorMsg)
		return nil, errors.New(errorMsg)
	}

	s.logger.Infof("Getting doctor %v", string(doctorBytes))

	var doctor careproto.Doctor
	json.Unmarshal(doctorBytes, &doctor)
	return &doctor, nil
}

func (s *DoctorServiceDefault) SaveDoctor(stub shim.ChaincodeStubInterface, doctor careproto.Doctor) (*careproto.Doctor, error) {
	fmt.Printf("Saving doctor %v \n", doctor)

	doctorBytes, err := json.Marshal(&doctor)

	doctorKey := "doctor:" + doctor.DoctorId
	err = stub.PutState(doctorKey, doctorBytes)
	if err != nil {
		s.logger.Errorf("Error while saving doctor '%v'. Error: %v", doctor, err)
		return nil, err
	}

	return &doctor, nil
}

func (s *DoctorServiceDefault) DecodeProtoByteString(encodedDoctorByteString string) (*careproto.Doctor, error) {
	var err error

	doctor := careproto.Doctor{}
	err = proto.Unmarshal([]byte(encodedDoctorByteString), &doctor)
	if err != nil {
		s.logger.Errorf("Error while unmarshalling Doctor: %v", err.Error())
	}

	return &doctor, err
}