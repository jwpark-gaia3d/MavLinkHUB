package com.paku.mavlinkhub.queue;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import com.paku.mavlinkhub.enums.APP_STATE;
import com.paku.mavlinkhub.enums.SOCKET_STATE;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class QueueIOBytes {

	@SuppressWarnings("unused")
	private static final String TAG = "QueueIOBytes";

	private final ArrayDeque<ByteBuffer> inputByteQueue;
	private final ArrayDeque<ByteBuffer> outputByteQueue;
	// hub wide msg center
	public Handler appMsgHandler;

	protected QueueIOBytes(Handler msgCenter, int capacity) {
		// to the device
		outputByteQueue = new ArrayDeque<ByteBuffer>(capacity);
		// from the device
		inputByteQueue = new ArrayDeque<ByteBuffer>(capacity);

		this.appMsgHandler = msgCenter;

	}

	// get bytes
	public ByteBuffer getOutputByteQueueItem() {
		synchronized (outputByteQueue) {
			return outputByteQueue.pollFirst();
		}
	}

	public ByteBuffer getInputByteQueueItem() {
		synchronized (inputByteQueue) {
			return inputByteQueue.pollFirst();
		}
	}

	// add bytes
	public void addInputByteQueueItem(Message byteMsg) {
		ByteBuffer buffer = ByteBuffer.wrap((byte[]) byteMsg.obj, 0, byteMsg.arg1);
		synchronized (inputByteQueue) {
			inputByteQueue.addLast(buffer);
		}
		return;

	}

	public void addInputByteQueueItem(ByteBuffer buffer) {
		synchronized (inputByteQueue) {
			inputByteQueue.addLast(buffer);
		}
		return;
	}

	public void addOutputByteQueueItem(ByteBuffer buffer) {
		synchronized (outputByteQueue) {
			outputByteQueue.addLast(buffer);
		}
	}

	// get queues
	public ArrayDeque<ByteBuffer> getInputByteQueue() {
		return inputByteQueue;
	}

	public ArrayDeque<ByteBuffer> getOutputByteQueue() {
		return outputByteQueue;
	}

	// that's the true ADD ,method for this class
	// this handler is called by the messages coming from any other class build
	// over the QueueIOBytes. Any bytes receiving thread sends a msg with the
	// buffer here to be stored in the underlying queue.
	// msg other then ADD are forwarded to the main app messenger
	protected Handler startInputQueueMsgHandler() {
		return new Handler(Looper.getMainLooper()) {
			public void handleMessage(Message byteMsg) {

				final SOCKET_STATE[] socketStates = SOCKET_STATE.values();
				switch (socketStates[byteMsg.what]) {

				// new client connected
				case MSG_SOCKET_TCP_SERVER_CLIENT_CONNECTED:
					appMsgHandler.obtainMessage(APP_STATE.MSG_SERVER_CLIENT_CONNECTED.ordinal(), byteMsg.arg1, byteMsg.arg2, byteMsg.obj).sendToTarget();
					break;

				// Client lost;
				case MSG_SOCKET_TCP_SERVER_CLIENT_DISCONNECTED:
					appMsgHandler.obtainMessage(APP_STATE.MSG_SERVER_CLIENT_DISCONNECTED.ordinal()).sendToTarget();
					break;

				// Received data
				case MSG_SOCKET_DATA_READY:
					addInputByteQueueItem(byteMsg);
					break;

				// closing so kill itself
				case MSG_SOCKET_CLOSED:
					removeMessages(0);
					break;
				default:
					super.handleMessage(byteMsg);
				}
			}

		};

	}
}