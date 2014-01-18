package com.paku.mavlinkhub.queue.endpoints.drone;

import java.io.IOException;

import com.paku.mavlinkhub.queue.endpoints.DroneClient;
import com.paku.mavlinkhub.utils.ThreadSocketReader;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class DroneClientBluetooth extends DroneClient {

	private static final String TAG = "DroneClientBluetooth";
	private static final int SIZEBUFF = 1024;

	private final BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	private BluetoothSocket mBluetoothSocket;

	private Handler handlerDroneMsgRead;

	private DroneClientBluetoothConnThread droneConnectingBluetoothThread;
	private ThreadSocketReader socketWorkerThreadBT;

	public DroneClientBluetooth(Handler messenger) {
		super(messenger, SIZEBUFF);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void startClient(String address) {

		// start connection threat
		if (mBluetoothAdapter == null) {
			return;
		}

		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
		// create and start BT specific connection thread
		droneConnectingBluetoothThread = new DroneClientBluetoothConnThread(this, mBluetoothAdapter, mBluetoothDevice);
		droneConnectingBluetoothThread.start();
		return;

	}

	public void startTransmission(BluetoothSocket socket) {

		mBluetoothSocket = socket;

		// start received bytes handler
		handlerDroneMsgRead = startInputQueueMsgHandler();

		// start receiver thread
		socketWorkerThreadBT = new ThreadSocketReader(mBluetoothSocket, handlerDroneMsgRead);
		socketWorkerThreadBT.start();
	}

	@Override
	public void stopClient() {
		Log.d(TAG, "Closing connection..");

		// stop handler
		if (handlerDroneMsgRead != null) {
			handlerDroneMsgRead.removeMessages(0);
		}

		// stop socket thread
		if (isConnected()) {
			socketWorkerThreadBT.stopMe();
		}
	}

	@Override
	public boolean isConnected() {
		if (mBluetoothSocket == null) {
			return false;
		}
		else {
			return mBluetoothSocket.isConnected();
		}

	}

	@Override
	public String getPeerName() {

		return (isConnected()) ? mBluetoothSocket.getRemoteDevice().getName() : "";

	}

	@Override
	public String getPeerAddress() {
		return (isConnected()) ? mBluetoothSocket.getRemoteDevice().getAddress() : "";

	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	@Override
	public String getMyName() {
		return mBluetoothAdapter.getName();
	}

	@Override
	public String getMyAddress() {
		return mBluetoothAdapter.getAddress();
	}

	@Override
	public boolean writeBytes(byte[] bytes) throws IOException {

		if (isConnected()) {
			socketWorkerThreadBT.writeBytes(bytes);
			return true;
		}
		return false;
	}

}
