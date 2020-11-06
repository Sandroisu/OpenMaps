package ru.slatinin.openmaps;

public interface DownloadProgressListener {
    void showDownloadPercentage(long percentage);
    void showErrorDownloading(String error);
    void showSingleSegmentDone(int segmentNumber);
    void handleCriticalError(String message);
    void showDownloadingDone(boolean atLeastOneSegmentDone);
}
