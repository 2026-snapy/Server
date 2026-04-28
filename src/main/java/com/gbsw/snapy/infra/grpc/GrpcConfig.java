package com.gbsw.snapy.infra.grpc;

import com.snapy.proto.feed.RecommendServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    @Value("${grpc.server.host}")
    private String host;

    @Value("${grpc.server.port}")
    private int port;

    @Bean
    public ManagedChannel grpcServerChannel() {
        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }

    @Bean
    public RecommendServiceGrpc.RecommendServiceBlockingStub recommendStub(ManagedChannel recommendChannel) {
        return RecommendServiceGrpc.newBlockingStub(recommendChannel);
    }
}
