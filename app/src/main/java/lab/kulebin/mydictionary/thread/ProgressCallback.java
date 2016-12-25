package lab.kulebin.mydictionary.thread;

public interface ProgressCallback<Progress> {

    void onProgressChanged(Progress progress);
}
