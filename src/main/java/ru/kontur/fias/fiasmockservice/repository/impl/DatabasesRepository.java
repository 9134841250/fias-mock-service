package ru.kontur.fias.fiasmockservice.repository.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.kontur.fias.fiasmockservice.repository.FiasRepository;
import ru.kontur.fias.fiasmockservice.service.ArrayOfDownloadFileInfo;
import ru.kontur.fias.fiasmockservice.service.DownloadFileInfo;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DatabasesRepository implements FiasRepository {

    private static final Pattern FIAS_DB = Pattern.compile("(?<version>\\d{4}-\\d{2}-\\d{2})_(dbf|xml)\\.rar");
    private static final DateTimeFormatter RU_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Value("${fias.databases.dir}") private String databasesDirectory;
    @Value("${start.from.version:2016-01-01}") private String startVersion;
    private Path directory;
    private final AtomicReference<LocalDate> currentVersion = new AtomicReference<>();
    private final AtomicInteger retries = new AtomicInteger();

    @PostConstruct
    public void init() {
        directory = Paths.get(databasesDirectory);
        final LocalDate startDate = LocalDate.parse(startVersion);
        final LocalDate firstVersion = getFirstVersion(startDate).orElse(startDate);
        currentVersion.set(firstVersion);
    }

    private Optional<LocalDate> getFirstVersion(final LocalDate fromDate) {
        try {
            return Files.walk(directory, 1, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.naturalOrder())
                    .map(path -> {
                        final Matcher matcher = FIAS_DB.matcher(path.getFileName().toString());
                        return matcher.matches() ? LocalDate.parse(matcher.group("version")) : null;
                    })
                    .filter(Objects::nonNull)
                    .filter(fromDate::isBefore)
                    .findFirst();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private DownloadFileInfo buildDownloadFileInfo(final LocalDate version) {
        final DownloadFileInfo fileInfo = new DownloadFileInfo();
        fileInfo.setTextVersion("БД ФИАС от " + version.format(RU_FORMATTER));

        final ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        final HttpServletRequest request = attributes.getRequest();
        final String path = request.getRequestURI();
        final StringBuffer builder = request.getRequestURL();

        final String host = builder.substring(0, builder.indexOf(path));

        final String isoVersion = version.format(DateTimeFormatter.ISO_DATE);
        fileInfo.setFiasCompleteDbfUrl(host + "/files/" + isoVersion + "_dbf.rar");
        fileInfo.setFiasCompleteXmlUrl(host + "/files/" + isoVersion + "_xml.rar");

        return fileInfo;
    }

    @Override
    public DownloadFileInfo get() {
        final boolean next = retries.incrementAndGet() % 3 == 0;
        final LocalDate version = currentVersion
                .getAndUpdate(previous -> next ? getFirstVersion(previous).orElse(previous) : previous);

        return buildDownloadFileInfo(version);
    }

    @Override
    public ArrayOfDownloadFileInfo getAll() {
        final List<LocalDate> versionsList;
        try {
            versionsList = Files.walk(directory, 1, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.naturalOrder())
                    .map(path -> {
                        final Matcher matcher = FIAS_DB.matcher(path.getFileName().toString());
                        return matcher.matches() ? LocalDate.parse(matcher.group("version")) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final ArrayOfDownloadFileInfo result = new ArrayOfDownloadFileInfo();
        versionsList.stream()
                .map(this::buildDownloadFileInfo)
                .forEach(fileInfo -> result.getDownloadFileInfo().add(fileInfo));
        return result;
    }
}
