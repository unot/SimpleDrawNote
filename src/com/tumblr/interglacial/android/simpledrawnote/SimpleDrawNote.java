package com.tumblr.interglacial.android.simpledrawnote;

import java.io.BufferedOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class SimpleDrawNote extends Activity {
	DrawNoteView view;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_simple_draw_note);
		view = new DrawNoteView(getApplication());
		setContentView(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_simple_draw_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear:
			view.clearDrawList();
			break;
		case R.id.save:
			view.saveToFile();
			break;
		case R.id.send:
			view.sendSocket();
		}
		return true;
	}
}

/** 描画クラス **/
class DrawNoteView extends View {
	Bitmap bmp = null;
	Canvas bmpCanvas;
	Point oldpos = new Point(-1, -1);
	EditText toAddress;
	private static final int PORT = 3017;

	// ArrayList<Point> draw_list = new ArrayList<Point>();
	public DrawNoteView(Context c) {
		super(c);
		setFocusable(true);
	}

	public void sendSocket() {
		toAddress = new EditText(null);
		new AlertDialog.Builder(getContext())
				.setTitle("送信先アドレス")
				.setView(toAddress)
				.setPositiveButton("Send",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// TODO 自動生成されたメソッド・スタブ
								try {
									Socket socket = new Socket(
											(InetAddress) toAddress.getText(),
											PORT);
									BufferedOutputStream out = new BufferedOutputStream(
											socket.getOutputStream());
									bmp.compress(CompressFormat.PNG, 100, out);
									if (out != null) {
										out.flush();
										out.close();
									}
									dialog.dismiss();
								} catch (Exception e) {
									e.getStackTrace();
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO 自動生成されたメソッド・スタブ
								dialog.cancel();
							}
						}).show();

	}

	public void clearDrawList() {
		bmpCanvas.drawColor(Color.WHITE);
		invalidate();
	}

	public void saveToFile() {
		// 画像をファイルに書き込み
		try {
			MediaStore.Images.Media.insertImage(getContext()
					.getContentResolver(), bmp, "", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 画面サイズが変更された時 **/
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bmpCanvas = new Canvas(bmp);
		bmpCanvas.drawColor(Color.WHITE);
	}

	public void onDraw(Canvas canvas) {
		canvas.drawBitmap(bmp, 0, 0, null);
	}

	/* タッチイベント */
	public boolean onTouchEvent(MotionEvent event) {
		// 描画位置の確認
		Point cur = new Point((int) event.getX(), (int) event.getY());
		if (oldpos.x < 0) {
			oldpos = cur;
		}
		// 描画属性
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeWidth(4);
		// 線を描画
		bmpCanvas.drawLine(oldpos.x, oldpos.y, cur.x, cur.y, paint);
		oldpos = cur;
		// 指を持ち上げたら座標をリセット
		if (event.getAction() == MotionEvent.ACTION_UP) {
			oldpos = new Point(-1, -1);
		}
		invalidate();
		return true;
	}
}