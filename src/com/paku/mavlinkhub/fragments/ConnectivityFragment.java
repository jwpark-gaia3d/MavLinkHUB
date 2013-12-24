package com.paku.mavlinkhub.fragments;

import java.util.ArrayList;
import java.util.Set;

import com.paku.mavlinkhub.R;
import com.paku.mavlinkhub.communication.CommunicationHUB;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectivityFragment extends Fragment {

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBluetoothDevice;
	Set<BluetoothDevice> pairedDevices;	
	
	private static final int REQUEST_ENABLE_BT = 123;
	
	ListView btDevListView;
	Button button;

	public ConnectivityFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View connView = inflater.inflate(R.layout.fragment_connectivity,
				container, false);

		btDevListView = (ListView) connView.findViewById(R.id.list_bt_bonded);
		button = (Button) connView.findViewById(R.id.button_connect);

		return connView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		pairedDevices = mBluetoothAdapter.getBondedDevices();

		button.setVisibility(View.INVISIBLE);		

		if (mBluetoothAdapter == null){
			
			Toast.makeText(
					getActivity().getApplicationContext(),
					"No Bluetooth Adapter found.",
					Toast.LENGTH_LONG).show();
			return;
			
		}
		else				
		if (!mBluetoothAdapter.isEnabled()) {

			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
	
	
		final ArrayList<String> bondedDevicesNameList = new ArrayList<String>();
		

		// If there are paired devices
		if (pairedDevices.size() > 0) {
			
			button.setVisibility(View.VISIBLE);
			
			bondedDevicesNameList.clear();
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// btDevListView
				bondedDevicesNameList.add(device.getName() + "\n"
						+ device.getAddress());
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity().getApplicationContext(),
					android.R.layout.simple_list_item_1,
					//android.R.layout.simple_dropdown_item_1line,
					bondedDevicesNameList.toArray(new String[0])) {

				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					View view = super.getView(position, convertView, parent);

					TextView textView = (TextView) view
							.findViewById(android.R.id.text1);
					textView.setTextColor(Color.DKGRAY);

					return view;
				}
			}

			;

			btDevListView.setAdapter(adapter);
			btDevListView.setOnItemClickListener(btListClickListener);

		} else {

			Toast.makeText(
					getActivity().getApplicationContext(),
					R.string.error_no_paired_bt_devices_found_pair_device_first,
					Toast.LENGTH_LONG).show();

		}

	}

	private final AdapterView.OnItemClickListener btListClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
                        
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
						
			CommunicationHUB comHUB = (CommunicationHUB) getActivity().getApplication();
			comHUB.ConnectBT(mBluetoothAdapter,mBluetoothDevice);
			

		}
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}}