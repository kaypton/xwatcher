package com.github.fenrir.xmessaging.drivers.nats.driver;

import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessagingListener;
import com.github.fenrir.xmessaging.XMessagingPublisher;
import com.github.fenrir.xmessaging.drivers.nats.driver.publisher.XNatsPublisher;
import com.github.fenrir.xmessaging.drivers.nats.driver.subscriber.XNatsListener;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XNatsDriverFactory implements Serializable {
    static private final Logger logger = LoggerFactory.getLogger("XNatsDriverFactory");

    /**
     * nats server addresses
     */
    @Getter @Setter private static List<String> natsServerAddresses = null;

    public static void init(String natsServerAddresses){
        String[] natsServerAddressesList = natsServerAddresses.split(",");
        List<String> addressesList = new ArrayList<>(Arrays.asList(natsServerAddressesList));
        setNatsServerAddresses(addressesList);
        // setNatsServerAddresses(new ArrayList<>(addressesList));
    }

    /**
     * create a listener use callback
     * @param topic message topic
     * @return XMessagingListener
     */
    public static XMessagingListener createListener(String topic){
        XNatsListener subscriber = XNatsListener.create(getNatsServerAddresses(),
                topic, XNatsSettings.ConsumeMode.Receive, null);
        subscriber.run();
        return subscriber;
    }

    /**
     * create a listener without callback
     * @param topic message topic
     * @param messageProcessCallBack callback object
     * @return XMessagingListener
     */
    public static XMessagingListener createListener(String topic,
                                             MessageProcessCallBack messageProcessCallBack){
        XNatsListener subscriber = XNatsListener.create(getNatsServerAddresses(),
                topic, XNatsSettings.ConsumeMode.CallBack, messageProcessCallBack);
        subscriber.run();
        return subscriber;
    }

    /**
     * create a publisher
     * @param topic message topic
     * @return XMessagingPublisher
     */
    public static XMessagingPublisher createPublisher(String topic){
        return XNatsPublisher.create(getNatsServerAddresses(),
                topic, XNatsSettings.PublishMode.Publish);
    }

    /* ------------------------------------------------------------ */

    private final List<String> _natsServerAddress;

    private XNatsDriverFactory(String _natsServerAddresses){
        String[] natsServerAddressesList = _natsServerAddresses.split(",");
        List<String> addressesList = new ArrayList<>(Arrays.asList(natsServerAddressesList));
        this._natsServerAddress = new ArrayList<>(addressesList);
    }

    public XMessagingListener getListener(String topic){
        XNatsListener subscriber = XNatsListener.create(this._natsServerAddress,
                topic, XNatsSettings.ConsumeMode.Receive, null);
        subscriber.run();
        return subscriber;
    }

    public XMessagingListener getListener(String topic,
                                          MessageProcessCallBack callBack){
        XNatsListener subscriber = XNatsListener.create(this._natsServerAddress,
                topic, XNatsSettings.ConsumeMode.CallBack, callBack);
        subscriber.run();
        return subscriber;
    }

    public XMessagingPublisher getPublisher(String topic){
        return XNatsPublisher.create(this._natsServerAddress,
                topic, XNatsSettings.PublishMode.Publish);
    }

    public static XNatsDriverFactory getInstance(String _natsServerAddresses){
        return new XNatsDriverFactory(_natsServerAddresses);
    }
}
