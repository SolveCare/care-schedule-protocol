package care.solve.protocol.schedule.service.impl;

import care.solve.fabric.service.HFClientFactory;
import care.solve.fabric.service.TransactionService;
import care.solve.protocol.schedule.entity.DoctorPublic;
import care.solve.protocol.schedule.entity.ScheduleProtos;
import care.solve.protocol.schedule.service.DoctorService;
import care.solve.protocol.schedule.transformer.DoctorToProtoCollectionTransformer;
import care.solve.protocol.schedule.transformer.DoctorToProtoTransformer;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class DoctorServiceImpl implements DoctorService {

    private TransactionService transactionService;
    private HFClientFactory hfClientFactory;
    private ChaincodeID chaincodeId;
    private Channel healthChannel;

    private DoctorToProtoTransformer doctorToProtoTransformer;
    private DoctorToProtoCollectionTransformer doctorToProtoCollectionTransformer;

    @Autowired
    public DoctorServiceImpl(TransactionService transactionService, HFClientFactory hfClientFactory, ChaincodeID chaincodeId, Channel healthChannel, DoctorToProtoTransformer doctorToProtoTransformer, DoctorToProtoCollectionTransformer doctorToProtoCollectionTransformer) {
        this.transactionService = transactionService;
        this.hfClientFactory = hfClientFactory;
        this.chaincodeId = chaincodeId;
        this.healthChannel = healthChannel;
        this.doctorToProtoTransformer = doctorToProtoTransformer;
        this.doctorToProtoCollectionTransformer = doctorToProtoCollectionTransformer;
    }

    @Override
    public DoctorPublic create(DoctorPublic doctorPublic) {
        return publishDoctorToChaincode(doctorPublic);
    }

    public DoctorPublic publishDoctorToChaincode(DoctorPublic doctor) {
        ScheduleProtos.DoctorPublic protoDoctor = doctorToProtoTransformer.transformToProto(doctor);
        String byteString = new String(protoDoctor.toByteArray());
        CompletableFuture<BlockEvent.TransactionEvent> futureEvents = transactionService.sendInvokeTransaction(
                hfClientFactory.getClient(),
                chaincodeId,
                healthChannel,
                healthChannel.getPeers(),
                "createDoctor",
                new String[]{byteString});

        ScheduleProtos.DoctorPublic savedProtoDoctor = null;
        try {
            byte[] payload = futureEvents.get().getTransactionActionInfo(0).getProposalResponsePayload();
            savedProtoDoctor = ScheduleProtos.DoctorPublic.parseFrom(payload);
        } catch (InterruptedException | InvalidProtocolBufferException | ExecutionException e) {
            e.printStackTrace();
        }

        return doctorToProtoTransformer.transformFromProto(savedProtoDoctor);
    }

    @Override
    public DoctorPublic get(String doctorId) throws IOException {
        ByteString protoDoctorByteString = transactionService.sendQueryTransaction(
                hfClientFactory.getClient(),
                chaincodeId,
                healthChannel,
                "getDoctor",
                new String[]{doctorId});

        ScheduleProtos.DoctorPublic protoDoctor = ScheduleProtos.DoctorPublic.parseFrom(protoDoctorByteString);
        return doctorToProtoTransformer.transformFromProto(protoDoctor);
    }

    @Override
    public List<DoctorPublic> getAll() throws IOException {
        ByteString protoDoctorsByteString = transactionService.sendQueryTransaction(
                hfClientFactory.getClient(),
                chaincodeId,
                healthChannel,
                "getAllDoctors",
                new String[]{});

        ScheduleProtos.DoctorCollection protoDoctor = ScheduleProtos.DoctorCollection.parseFrom(protoDoctorsByteString);
        return doctorToProtoCollectionTransformer.transformFromProto(protoDoctor);
    }
}
