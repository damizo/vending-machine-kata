package common;

import domain.ObservableTransaction;

public interface Observer {

    void notifyAboutEndOfTransaction (ObservableTransaction observable);
}
