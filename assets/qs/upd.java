import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.MotionEvent;
import android.os.Handler;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
String getPropValue(String key) {
try {
String propText=readFileText(appPath+"/info.prop");
if (propText.isEmpty()) return "";
String[] lines=propText.split("\n");
for (String line:lines) {
line=line.trim();
if (line.startsWith(key+"=")) return line.substring(key.length()+1);
}
} catch (Exception e) {
error(e);
}
return "";
}
void k(String msg, String url) {
Activity activity=getActivity();
if (activity==null) return;
activity.runOnUiThread(new Runnable() {
public void run() {
TextView titleTextView=new TextView(activity);
titleTextView.setText(msg);
titleTextView.setTextColor(Color.BLACK);
titleTextView.setPadding(0,0,0,16);
LinearLayout layout=new LinearLayout(activity);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40,40,40,40);
layout.addView(titleTextView);
AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
dialogBuilder.setCancelable(false)
.setTitle("发现新版本")
.setView(layout)
.setPositiveButton("更新", null);
AlertDialog dialog=dialogBuilder.create();
dialog.show();
View btn=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
btn.setTextColor(Color.parseColor("#007aff"));
btn.setOnTouchListener(new View.OnTouchListener() {
Handler h=new Handler();
boolean closed=false;
Runnable closeRun=new Runnable(){
public void run(){
closed=true;
dialog.dismiss();
activity.runOnUiThread(new Runnable(){
public void run(){
toast("已跳过本次更新");
}
});
}
};
public boolean onTouch(View v, MotionEvent e) {
if(e.getAction()==MotionEvent.ACTION_DOWN){
closed=false;
h.postDelayed(closeRun,500);
}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
h.removeCallbacks(closeRun);
if(!closed){
new Thread(new Runnable(){
public void run(){
String zip=appPath+"/Java.zip";
try {
httpDownload(url,zip);
decompressFile(zip);
new File(zip).delete();
activity.runOnUiThread(new Runnable(){
public void run(){
toast("更新成功,请重新加载脚本");
}
});
} catch (Exception er) {
error(er);
}
}
}).start();
}
dialog.dismiss();
}
return true;
}
});
GradientDrawable bg=new GradientDrawable();
bg.setColor(Color.WHITE);
bg.setCornerRadius(20);
dialog.getWindow().setBackgroundDrawable(bg);
}
});
}
void extractFile(ZipInputStream zis, String path) throws Exception {
BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(path));
byte[] buf=new byte[1024];int len;
while ((len=zis.read(buf))!=-1) bos.write(buf,0,len);
bos.close();
}
void decompressFile(String zipPath) throws Exception {
File zip=new File(zipPath);
String dir=zip.getParent();
FileInputStream fis=new FileInputStream(zip);
ZipInputStream zis=new ZipInputStream(fis);
ZipEntry entry;
while ((entry=zis.getNextEntry())!=null) {
String full=dir+File.separator+entry.getName();
if(entry.isDirectory()) new File(full).mkdir();
else extractFile(zis,full);
zis.closeEntry();
}
zis.close();fis.close();
}
void onLoad() {
new Thread(new Runnable() {
public void run() {
try {
String jsonStr=httpGet("https://aur2.github.io/assets/qs/upd.json");
JSONObject json=new JSONObject(jsonStr);
String remoteVer=json.getString("bb");
String log=json.getString("nr");
String dlUrl=json.getString("url");
String local=getPropValue("version");
if (!local.isEmpty()&&!local.equals(remoteVer)) {
Activity act=getActivity();
if(act!=null) act.runOnUiThread(new Runnable(){
public void run(){
k(log,dlUrl);
}
});
}
} catch (Exception e) {
error(e);
}
}
}).start();
}