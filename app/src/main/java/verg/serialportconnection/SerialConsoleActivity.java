package verg.serialportconnection;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_serial_console)
public class SerialConsoleActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {
    private final String TAG = SerialConsoleActivity.class.getSimpleName();
    @ViewById
    Toolbar toolbar;
    @ViewById
    FloatingActionButton sent;
    @ViewById
    AppCompatEditText input;
    @ViewById
    NestedScrollView demoScroller;
    @ViewById
    TextView consoleText;
    @SystemService
    UsbManager mUsbManager;
    @Extra(UsbManager.EXTRA_DEVICE)
    @InstanceState
    UsbDevice usbDevice;
    UsbSerialDriver sDriver;
    SerialConsole serialConsole;
    private boolean isOpen;

    void getDriver() {
        //when first in,had Intent
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.probeSingleDevice(mUsbManager, usbDevice);
        Log.d(TAG, "Found usb device: " + usbDevice);
        if (drivers.isEmpty()) {
            Log.d(TAG, "  - No UsbSerialDriver available.");
            return;
        }

        sDriver = drivers.get(0);

        serialConsole = new SerialConsole(sDriver, this);

        isOpen = serialConsole.open(115200, 8, UsbSerialDriver.STOPBITS_1,
                UsbSerialDriver.PARITY_NONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getDriver();
    }

    @AfterViews
    void afterViews() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (!isOpen) {
            consoleText.setText("No serial device.");
        } else {
            consoleText.setText("Serial device: " + sDriver.getClass().getSimpleName() + "\n");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        serialConsole.close(); // 关闭串口
    }

    @Click
    void sent() {
        if (serialConsole != null) {
            serialConsole.wirte((input.getText() + "\n").getBytes());
        }
    }

    @UiThread
    @Override
    public void onNewData(byte[] data) {
        consoleText.append(new String(data));
        demoScroller.smoothScrollTo(0, consoleText.getBottom());
    }

    @Override
    public void onRunError(Exception e) {

    }

    @Receiver(actions = UsbManager.ACTION_USB_DEVICE_ATTACHED)
    void device_attached(@Receiver.Extra(UsbManager.EXTRA_DEVICE) UsbDevice usbDevice) {
//        usbListAdapter.attachedDevice(usbDevice);
        Toast.makeText(this, "device_attached", Toast.LENGTH_SHORT).show();
    }

    @Receiver(actions = UsbManager.ACTION_USB_DEVICE_DETACHED)
    void device_detached(@Receiver.Extra(UsbManager.EXTRA_DEVICE) UsbDevice usbDevice) {
//        usbListAdapter.detachedDevice(usbDevice);
        Toast.makeText(this, "device_detached", Toast.LENGTH_SHORT).show();
    }


}
