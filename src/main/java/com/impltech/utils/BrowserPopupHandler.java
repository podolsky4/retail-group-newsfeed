package com.impltech.utils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.util.Callback;

import java.awt.*;
import java.net.URI;

public class BrowserPopupHandler implements Callback<PopupFeatures, WebEngine> {

    private WebEngine popupHandlerEngine;

    @Override
    public WebEngine call(PopupFeatures popupFeatures) {
        return getPopupHandler();
    }

    private WebEngine getPopupHandler()
    {
        if (popupHandlerEngine == null) // lazy init - so we only initialize it when needed ...
        {
            synchronized (this) // double checked synchronization
            {
                if (popupHandlerEngine == null)
                {
                    popupHandlerEngine = initEngine();
                }
            }
        }
        return popupHandlerEngine;
    }
    private WebEngine initEngine()
    {
        final WebEngine popupHandlerEngine = new WebEngine();

        // this change listener will trigger when our secondary popupHandlerEngine starts to load the url ...
        popupHandlerEngine.locationProperty().addListener(new ChangeListener<String>()
     {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String location)
            {
                if (!location.isEmpty())
                {
                    Platform.runLater(new Runnable()
                    {

                        public void run()
                        {
//                            popupHandlerEngine.loadContent(""); // stop loading and unload the url
                            // -> does this internally: popupHandlerEngine.getLoadWorker().cancelAndReset();
                        }

                    });

                    try
                    {
                        // Open URL in Browser:
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE))
                        {
                            URI uri = new URI(location);
                            desktop.browse(uri);
                        }
                        else
                        {
                            System.out.println("Could not load URL: " + location);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

        });
        return popupHandlerEngine;
    }

}
