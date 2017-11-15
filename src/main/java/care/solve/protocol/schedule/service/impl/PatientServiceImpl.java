package care.solve.protocol.schedule.service.impl;


import care.solve.fabric.service.TransactionService;
import care.solve.protocol.schedule.entity.Patient;
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
    private Channel healthChannel;

    private PatientToProtoTransformer patientToProtoTransformer;

    @Autowired
    public PatientServiceImpl(TransactionService transactionService, HFClient peerAdminHFClient, Channel healthChannel, PatientToProtoTransformer patientToProtoTransformer) {
        this.transactionService = transactionService;
        this.peerAdminHFClient = peerAdminHFClient;
        this.healthChannel = healthChannel;
        this.patientToProtoTransformer = patientToProtoTransformer;
    }

    @Override
    public Patient create(Patient patient) throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        return publishPatientToChaincode(patient);
    }

    public Patient publishPatientToChaincode(Patient patient) throws ExecutionException, InterruptedException, InvalidProtocolBufferException {
        ScheduleProtos.Patient protoPatient = patientToProtoTransformer.transformToProto(patient);
        String byteString = new String(protoPatient.toByteArray());
        CompletableFuture<BlockEvent.TransactionEvent> futureEvents = transactionService.sendInvokeTransaction(
                peerAdminHFClient,
                healthChannel,
                healthChannel.getPeers(),
                ScheduleProtos.PatientFunctions.PATIENT_CREATE.name(),
                new String[]{byteString});

        byte[] payload = futureEvents.get().getTransactionActionInfo(0).getProposalResponsePayload();
        ScheduleProtos.Patient savedProtoPatient = ScheduleProtos.Patient.parseFrom(payload);
        return patientToProtoTransformer.transformFromProto(savedProtoPatient);
    }

    @Override
    public Patient get(String patientId) throws IOException {
        ByteString protoPatientByteString = transactionService.sendQueryTransaction(
                peerAdminHFClient,
                healthChannel,
                ScheduleProtos.PatientFunctions.PATIENT_GET_BY_ID.name(),
                new String[]{patientId});

        ScheduleProtos.Patient protoPatient = ScheduleProtos.Patient.parseFrom(protoPatientByteString);
        return patientToProtoTransformer.transformFromProto(protoPatient);
    }
}
