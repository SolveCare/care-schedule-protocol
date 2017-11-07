package care.solve.protocol.schedule.service.impl;


import care.solve.fabric.service.TransactionService;
import care.solve.protocol.schedule.entity.PatientPublic;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import care.solve.protocol.schedule.service.PatientService;
import care.solve.protocol.schedule.transformer.PatientToProtoTransformer;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class PatientServiceImpl implements PatientService {

    private TransactionService transactionService;
    private HFClient peerAdminHFClient;
    private ChaincodeID chaincodeId;
    private Channel healthChannel;

    private PatientToProtoTransformer patientToProtoTransformer;

    @Autowired
    public PatientServiceImpl(TransactionService transactionService, HFClient peerAdminHFClient, ChaincodeID chaincodeId, Channel healthChannel, PatientToProtoTransformer patientToProtoTransformer) {
        this.transactionService = transactionService;
        this.peerAdminHFClient = peerAdminHFClient;
        this.chaincodeId = chaincodeId;
        this.healthChannel = healthChannel;
        this.patientToProtoTransformer = patientToProtoTransformer;
    }

    @Override
    public PatientPublic create(PatientPublic patientPublic) throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        return publishPatientToChaincode(patientPublic);
    }

    public PatientPublic publishPatientToChaincode(PatientPublic patientPublic) throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        ScheduleProtos.PatientPublic protoPatient = patientToProtoTransformer.transformToProto(patientPublic);
        String byteString = new String(protoPatient.toByteArray());
        CompletableFuture<BlockEvent.TransactionEvent> futureEvents = transactionService.sendInvokeTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                healthChannel.getPeers(),
                "createPatient",
                new String[]{byteString});

        byte[] payload = futureEvents.get().getTransactionActionInfo(0).getProposalResponsePayload();
        ScheduleProtos.PatientPublic savedProtoPatient = ScheduleProtos.PatientPublic.parseFrom(payload);
        return patientToProtoTransformer.transformFromProto(savedProtoPatient);
    }

    @Override
    public PatientPublic get(String patientId) throws IOException {
        ByteString protoPatientByteString = transactionService.sendQueryTransaction(
                peerAdminHFClient,
                chaincodeId,
                healthChannel,
                "getPatient",
                new String[]{patientId});

        ScheduleProtos.PatientPublic protoPatient = ScheduleProtos.PatientPublic.parseFrom(protoPatientByteString);
        return patientToProtoTransformer.transformFromProto(protoPatient);
    }
}
