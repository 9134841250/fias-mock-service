package ru.kontur.fias.fiasmockservice.repository;

import ru.kontur.fias.fiasmockservice.service.ArrayOfDownloadFileInfo;
import ru.kontur.fias.fiasmockservice.service.DownloadFileInfo;

public interface FiasRepository {

    DownloadFileInfo get();

    ArrayOfDownloadFileInfo getAll();
}
