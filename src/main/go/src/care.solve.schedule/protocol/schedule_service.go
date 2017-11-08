package protocol

import (
	careproto "./proto"
	"github.com/hyperledger/fabric/core/chaincode/shim"
)

type ScheduleService interface {

	GetScheduleByOwnerId(stub shim.ChaincodeStubInterface, ownerId string) (*careproto.Schedule, error)
	CreateSchedule(stub shim.ChaincodeStubInterface, schedule careproto.Schedule) (*careproto.Schedule, error)
	CreateSlot(stub shim.ChaincodeStubInterface, scheduleId string, slot careproto.Slot) (*careproto.Slot, error)
	UpdateSlot(stub shim.ChaincodeStubInterface, scheduleId string, slotId string, newSlot careproto.Slot) error
	DecodeScheduleByteString(scheduleByteString string) (*careproto.Schedule, error)
	DecodeSlotByteString(scheduleByteString string) (*careproto.Slot, error)

}