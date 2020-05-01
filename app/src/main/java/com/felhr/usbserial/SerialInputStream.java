package com.felhr.usbserial;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;

import ca.ergotera.remote_ir.misc.Logger;

public class SerialInputStream extends InputStream implements UsbSerialInterface.UsbReadCallback
{
    private static final String CLASS_ID = SerialInputStream.class.getSimpleName();

    protected final UsbSerialInterface device;
    protected ArrayBlockingQueue data = new ArrayBlockingQueue<Integer>(256);
    protected volatile boolean is_open;

    public SerialInputStream(UsbSerialInterface device)
    {
        this.device = device;
        is_open = true;
        device.read(this);
    }

    @Override
    public int read()
    {
        while (is_open)
        {
            try
            {
                return (Integer)data.take();
            } catch (InterruptedException e)
            {
                // ignore, will be retried by while loop
            }
        }
        return -1;
    }

    public void close()
    {
        is_open = false;
        try
        {
            data.put(-1);
        } catch (InterruptedException e)
        {
            Logger.Error(CLASS_ID, e.getMessage());
        }
    }

    public void onReceivedData(byte[] new_data)
    {
        for (byte b : new_data)
        {
            try
            {
                data.put(((int)b) & 0xff);
            } catch (InterruptedException e)
            {
                // ignore, possibly losing bytes when buffer is full
            }
        }
    }
}
