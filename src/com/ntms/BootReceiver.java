package com.ntms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	static final String action_boot = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(action_boot)) {
			Intent ootStartIntent = new Intent(context, MainActivity.class);
			ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// ����Activity
																	// Affinity�ж��Ƿ���Ҫ�����µ�Task��Ȼ���ٴ����µ�Activityʵ���Ž�ȥ
			context.startActivity(ootStartIntent);
		}
	}
}
