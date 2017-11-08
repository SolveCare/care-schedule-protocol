package protocol

import (
	careproto "./proto"
	"github.com/hyperledger/fabric/core/chaincode/shim"
)


type DoctorService interface {

	GetAllDoctors(stub shim.ChaincodeStubInterface) ([]*careproto.Doctor, error)
	GetDoctorById(stub shim.ChaincodeStubInterface, doctorId string) (*careproto.Doctor, error)
	SaveDoctor(stub shim.ChaincodeStubInterface, doctor careproto.Doctor) (*careproto.Doctor, error)
	DecodeProtoByteString(encodedDoctorByteString string) (*careproto.Doctor, error)

}
