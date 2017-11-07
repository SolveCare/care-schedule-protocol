package care.solve.protocol.schedule.service.impl;

import care.solve.fabric.service.TransactionService;
import care.solve.protocol.schedule.entity.Schedule;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import care.solve.protocol.schedule.entity.Slot;
import care.solve.protocol.schedule.service.ScheduleService;
import care.solve.protocol.schedule.transformer.ScheduleTransformer;
import care.solve.protocol.schedule.transformer.SlotTransformer;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private TransactionService transactionService;
    private HFClient peerAdminHFClient;
    private ChaincodeID chaincodeId;
    private Channel healthChannel;

    private ScheduleTransformer scheduleTransformer;
    private SlotTransformer slotTransformer;

    @Autowired
    public ScheduleServiceImpl(TransactionService transactionService, HFClient peerAdminHFClient, ChaincodeID chaincodeId, Channel healthChannel, ScheduleTransformer scheduleTransformer, SlotTransformer slotTransformer) {
        this.transactionService = transactionService;
        this.peerAdminHFClient = peerAdminHFClient;
        this.chaincodeId = chaincodeId;
        this.healthChannel = healthChannel;
        this.scheduleTransformer = scheduleTransformer;
        this.slotTransformer = slotTransformer;
    }

    @Override
    public Schedule createSchedule(Schedule schedule) {
        ScheduleProtos.Schedule scheduleProto = scheduleTransformer.transformToProto(schedule);

//        String byteString = TextFormat.printToString(scheduleProto);
        String byteString = new String(scheduleProto.toByteArray());
        CompletableFuture<BlockEvent.TransactionEvent> futureEvents = transactionService.sendInvokeTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                healthChannel.getPeers(),
                "createSchedule",
                new String[]{byteString});

        ScheduleProtos.Schedule savedSchedule = null;
        try {
            byte[] payload = futureEvents.get().getTransactionActionInfo(0).getProposalResponsePayload();
            savedSchedule = ScheduleProtos.Schedule.parseFrom(payload);
        } catch (InterruptedException | InvalidProtocolBufferException | ExecutionException e) {
            e.printStackTrace();
        }

        return scheduleTransformer.transformFromProto(savedSchedule);
    }

    @Override
    public Slot createSlot(String scheduleId, Slot slot) {
        ScheduleProtos.Slot protoSlot = slotTransformer.transformToProto(slot);

        String byteString = TextFormat.printToString(protoSlot);
        CompletableFuture<BlockEvent.TransactionEvent> futureEvents = transactionService.sendInvokeTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                healthChannel.getPeers(),
                "createSlot",
                new String[]{scheduleId, byteString});

        ScheduleProtos.Slot savedSlot = null;
        try {
            byte[] payload = futureEvents.get().getTransactionActionInfo(0).getProposalResponsePayload();
            savedSlot = ScheduleProtos.Slot.parseFrom(payload);
        } catch (InterruptedException | InvalidProtocolBufferException | ExecutionException e) {
            e.printStackTrace();
        }
        return slotTransformer.transformFromProto(savedSlot);
    }

    @Override
    public void updateSlot(String scheduleId, String slotId, Slot slot) {
        ScheduleProtos.Slot protoSlot = slotTransformer.transformToProto(slot);

        String byteString = TextFormat.printToString(protoSlot);
        transactionService.sendInvokeTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                healthChannel.getPeers(),
                "updateSlot",
                new String[]{scheduleId, slotId, byteString});
    }

    @Override
    public Schedule getSchedule(String ownerId) throws IOException, ChaincodeEndorsementPolicyParseException {
        ByteString protoScheduleByteString = transactionService.sendQueryTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                "getSchedule",
                new String[]{ownerId});

        ScheduleProtos.Schedule protoSchedule = ScheduleProtos.Schedule.parseFrom(protoScheduleByteString);
        return scheduleTransformer.transformFromProto(protoSchedule);
    }


//    public void create(RegistrationInfo request) throws ExecutionException, InterruptedException {
//
//        ScheduleProtos.ScheduleRequest protoScheduleRequest = scheduleRequestTransformer.transformToProto(request);
////        String byteString = new String(protoScheduleRequest.toByteArray()); // todo: investigate why we cannot unmarshall Slot object on the go side
//        String byteString = TextFormat.printToString(protoScheduleRequest);
//        transactionService.sendInvokeTransaction(
//                client,
//                chaincodeId,
//                healthChannel,
//                peer0,
//                "registerToDoctor",
//                new String[]{byteString}
//        );
//    }
}