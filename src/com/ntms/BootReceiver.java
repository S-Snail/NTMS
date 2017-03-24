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
			ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 根据Activity
																	// Affinity判断是否需要创建新的Task，然后再创建新的Activity实例放进去
			context.startActivity(ootStartIntent);
		}
	}
}
