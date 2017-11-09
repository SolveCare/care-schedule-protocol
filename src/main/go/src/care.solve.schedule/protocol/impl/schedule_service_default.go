package impl

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	"github.com/golang/protobuf/proto"
	"encoding/json"

	careproto "../proto"
	careprotocol "../../protocol"
)

type ScheduleServiceDefault struct {
	logger *shim.ChaincodeLogger

	scheduler careprotocol.Scheduler
}

func NewScheduleService(scheduler careprotocol.Scheduler) careprotocol.ScheduleService {
	var logger = shim.NewLogger("schedule_service")
	return &ScheduleServiceDefault{logger, scheduler}
}

func (s *ScheduleServiceDefault) GetScheduleByOwnerId(stub shim.ChaincodeStubInterface, ownerId string) (*careproto.Schedule, error) {
	schedule, err := s.scheduler.GetByOwnerId(stub, ownerId)
	if err != nil {
		return nil, err
	}

	return schedule, err
}

func (s *ScheduleServiceDefault) CreateSchedule(stub shim.ChaincodeStubInterface, schedule careproto.Schedule) (*careproto.Schedule, error) {
	savedSchedule, err := s.scheduler.Save(stub, schedule)
	if err != nil {
		return nil, err
	}

	return savedSchedule, nil
}

func (s *ScheduleServiceDefault) CreateSlot(stub shim.ChaincodeStubInterface, scheduleId string, slot careproto.Slot) (*careproto.Slot, error) {
	var err error

	schedule, err := s.scheduler.GetByOwnerId(stub, scheduleId)
	if err != nil {
		return nil, err
	}

	s.logger.Infof("Add new slot: %v to schedule %v", slot, scheduleId)

	schedule.Slots = append(schedule.Slots, &slot)
	s.logger.Infof("schedule.Slots: %v", schedule.Slots)

	jsonSchedule, err := json.Marshal(schedule)
	s.logger.Infof("jsonSchedule: %v", string(jsonSchedule))
	if err != nil {
		s.logger.Errorf("Error while marshalling Schedule: %v", err.Error())
		return nil, err
	}

	err = stub.PutState(s.scheduler.ConstructScheduleKey(scheduleId), jsonSchedule)
	if err != nil {
		s.logger.Errorf("Error while updating Schedule: %v", err.Error())
		return nil, err
	}

	return &slot, nil
}

func (s *ScheduleServiceDefault) UpdateSlot(stub shim.ChaincodeStubInterface, scheduleId string, slotId string, newSlot careproto.Slot) error {
	var err error

	schedule, err := s.scheduler.GetByOwnerId(stub, scheduleId)
	if err != nil {
		s.logger.Errorf("Error while retrieving Schedule: %v", err.Error())
		return err
	}

	for i, currentSlot := range schedule.Slots {
		if currentSlot.SlotId == slotId {
			existedSlot := schedule.Slots[i];
			if newSlot.TimeStart > 0 {
				existedSlot.TimeStart = newSlot.TimeStart
			}
			if newSlot.TimeFinish > 0 {
				existedSlot.TimeFinish = newSlot.TimeFinish
			}

			if newSlot.RegistrationInfo != nil && newSlot.RegistrationInfo.AttendeeId != "" {
				newSlot.Avaliable = careproto.Slot_BUSY
			} else {
				existedSlot.Avaliable = newSlot.Avaliable
			}
			existedSlot.RegistrationInfo = newSlot.RegistrationInfo
			break
		}
	}

	jsonSchedule, err := json.Marshal(schedule)
	if err != nil {
		s.logger.Errorf("Error while marshalling Schedule: %v", err.Error())
		return err
	}

	err = stub.PutState(s.scheduler.ConstructScheduleKey(scheduleId), jsonSchedule)
	if err != nil {
		s.logger.Errorf("Error while updating Schedule: %v", err.Error())
		return err
	}

	return nil
}

func (s *ScheduleServiceDefault) DecodeScheduleByteString(scheduleByteString string) (*careproto.Schedule, error) {
	var err error

	schedule := careproto.Schedule{}
	err = proto.Unmarshal([]byte(scheduleByteString), &schedule)
	if err != nil {
		s.logger.Errorf("Error while unmarshalling Schedule: %v", err.Error())
	}

	return &schedule, err
}

func (s *ScheduleServiceDefault) DecodeSlotByteString(scheduleByteString string) (*careproto.Slot, error) {
	var err error

	slot := careproto.Slot{}
	err = proto.UnmarshalText(scheduleByteString, &slot)
	if err != nil {
		s.logger.Errorf("Error while unmarshalling Slot: %v", err.Error())
	}

	return &slot, err
}