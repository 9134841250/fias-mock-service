package ru.kontur.fias.fiasmockservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.kontur.fias.fiasmockservice.repository.FiasRepository;
import ru.kontur.fias.fiasmockservice.service.*;

@Endpoint
public class FiasEndpoint {

    private static final String NAMESPACE_URI = "http://fias.nalog.ru/WebServices/Public/DownloadService.asmx";

    private final FiasRepository repository;
    private final ObjectFactory objectFactory = new ObjectFactory();

    @Autowired
    public FiasEndpoint(final FiasRepository repository) {
        this.repository = repository;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAllDownloadFileInfo")
    @ResponsePayload
    public GetAllDownloadFileInfoResponse getAllFilesInfo(@RequestPayload GetAllDownloadFileInfo request) {
        final GetAllDownloadFileInfoResponse response = new ObjectFactory().createGetAllDownloadFileInfoResponse();
        response.setGetAllDownloadFileInfoResult(repository.getAll());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetLastDownloadFileInfo")
    @ResponsePayload
    public GetLastDownloadFileInfoResponse getLastFileInfo(@RequestPayload GetLastDownloadFileInfo request) {
        final GetLastDownloadFileInfoResponse response = objectFactory.createGetLastDownloadFileInfoResponse();
        response.setGetLastDownloadFileInfoResult(repository.get());
        return response;
    }
}
