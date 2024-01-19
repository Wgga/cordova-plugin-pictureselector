package com.gua.pictureselector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.dialog.RemindDialog;
import com.luck.picture.lib.engine.CompressFileEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.interfaces.OnCallbackListener;
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener;
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener;
import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener;
import com.luck.picture.lib.permissions.PermissionConfig;
import com.luck.picture.lib.permissions.PermissionUtil;
import com.luck.picture.lib.style.BottomNavBarStyle;
import com.luck.picture.lib.style.PictureSelectorStyle;
import com.luck.picture.lib.style.PictureWindowAnimationStyle;
import com.luck.picture.lib.style.SelectMainStyle;
import com.luck.picture.lib.style.TitleBarStyle;
import com.luck.picture.lib.utils.DateUtils;
import com.luck.picture.lib.utils.DensityUtil;
import com.luck.picture.lib.utils.PictureFileUtils;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.widget.MediumBoldTextView;
import com.gua.pictureselector.GlideEngine;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnNewCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * This class echoes a string called from JavaScript.
 */
public class pictureselector extends CordovaPlugin {

    private final static String TAG = "PictureSelector";
    private final static String TAG_EXPLAIN_VIEW = "TAG_EXPLAIN_VIEW";
    private PictureSelectorStyle selectorStyle;
    private static String pkgName;
    private static Resources resource;

    private CallbackContext callbackContext;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();

        Context appContext = this.cordova.getActivity().getApplicationContext();
        resource = appContext.getResources();
        pkgName = appContext.getPackageName();

        selectorStyle = new PictureSelectorStyle();
        this.setSelectMainStyle();
        this.setSelectStyle();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getPictures")) {
            JSONObject params = args.getJSONObject(0);
            this.getPictures(params, callbackContext);
            return true;
        }
        return false;
    }

    private void getPictures(JSONObject params, CallbackContext callbackContext) throws JSONException {
        PictureSelector.create(cordova.getActivity())
        .openGallery(SelectMimeType.ofImage())
        .setImageEngine(GlideEngine.createGlideEngine())
        .setSelectorUIStyle(selectorStyle)
        .setMaxSelectNum(params.getInt("maxSelectNum"))
        .setSelectionMode(params.getInt("selectionMode"))
        .setCompressEngine(getCompressFileEngine(params.getBoolean("isCompress")))
        .setPermissionDeniedListener(getPermissionDeniedListener(params.getBoolean("isPermission")))
        .setPermissionDescriptionListener(getPermissionDescriptionListener(params.getBoolean("isPermission")))
        .isDisplayCamera(params.getBoolean("isShowCamera"))
        .isPreviewImage(params.getBoolean("isShowPreview"))
        .isOpenClickSound(params.getBoolean("isOpenSound"))
        .isFastSlidingSelect(params.getBoolean("isSlideSelect"))
        .isPreviewFullScreenMode(params.getBoolean("isPreviewFull"))
        .isMaxSelectEnabledMask(params.getBoolean("isMaxSelectMask"))
        .forResult(new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(ArrayList<LocalMedia> result) {
                ArrayList imageObjects = new ArrayList();
                for (LocalMedia media : result) {
                    if(!TextUtils.isEmpty(media.getRealPath())){
                        try {
                            JSONObject obj = new JSONObject();
                            String compresspath = !TextUtils.isEmpty(media.getCompressPath()) ? media.getCompressPath() : media.getRealPath();
                            obj.put("name", media.getFileName());
                            obj.put("path", media.getRealPath());
                            obj.put("compresspath", compresspath);
                            obj.put("time", media.getDateAddedTime());
                            obj.put("size", PictureFileUtils.formatAccurateUnitFileSize(media.getSize()));
                            obj.put("type", media.getMimeType());
                            imageObjects.add(obj);
                        } catch (Exception e) {
                            Log.getStackTraceString(e);
                        }
                    };
                }
                try {
                    JSONArray images = new JSONArray(imageObjects);
                    JSONObject obj = new JSONObject();
                    obj.put("images", images);
                    callbackContext.success(obj);
                } catch (Exception e) {
                    Log.getStackTraceString(e);
                }
            }

          @Override
            public void onCancel() {
                callbackContext.error("取消选择");
            }
        });
    }

    /**
     * 自定义选择器主题样式(仿微信-全新风格)
     */
    public static void setSelectMainStyle() {
        Context context = cordova.getContext();
        int num_selector = resource.getIdentifier("ps_default_num_selector", "drawable", pkgName);
        int checkbox_selector = resource.getIdentifier("ps_preview_checkbox_selector", "drawable", pkgName);
        int complete_normal_bg = resource.getIdentifier("ps_select_complete_normal_bg", "drawable", pkgName);
        int color_53575e = resource.getIdentifier("ps_color_53575e", "color", pkgName);
        int send = resource.getIdentifier("ps_send", "string", pkgName);
        int gallery_bg = resource.getIdentifier("ps_preview_gallery_bg", "drawable", pkgName);
        int select = resource.getIdentifier("ps_select", "string", pkgName);
        int color_white = resource.getIdentifier("ps_color_white", "color", pkgName);
        int complete_bg = resource.getIdentifier("ps_select_complete_bg", "drawable", pkgName);
        int send_num = resource.getIdentifier("ps_send_num", "string", pkgName);
        int color_black = resource.getIdentifier("ps_color_black", "color", pkgName);
        int album_bg = resource.getIdentifier("ps_album_bg", "drawable", pkgName);
        int grey_arrow = resource.getIdentifier("ps_ic_grey_arrow", "drawable", pkgName);
        int normal_back = resource.getIdentifier("ps_ic_normal_back", "drawable", pkgName);
        int color_half_grey = resource.getIdentifier("ps_color_half_grey", "color", pkgName);
        int color_9b = resource.getIdentifier("ps_color_9b", "color", pkgName);
        int preview = resource.getIdentifier("ps_preview", "string", pkgName);
        int preview_num = resource.getIdentifier("ps_preview_num", "string", pkgName);


        SelectMainStyle numberSelectMainStyle = new SelectMainStyle();
        numberSelectMainStyle.setSelectNumberStyle(true);
        numberSelectMainStyle.setPreviewSelectNumberStyle(false);
        numberSelectMainStyle.setPreviewDisplaySelectGallery(true);
        numberSelectMainStyle.setSelectBackground(num_selector);
        numberSelectMainStyle.setPreviewSelectBackground(checkbox_selector);
        numberSelectMainStyle.setSelectNormalBackgroundResources(complete_normal_bg);
        numberSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, color_53575e));
        numberSelectMainStyle.setSelectNormalText(send);
        numberSelectMainStyle.setAdapterPreviewGalleryBackgroundResource(gallery_bg);
        numberSelectMainStyle.setAdapterPreviewGalleryItemSize(DensityUtil.dip2px(context, 52));
        numberSelectMainStyle.setPreviewSelectText(select);
        numberSelectMainStyle.setPreviewSelectTextSize(14);
        numberSelectMainStyle.setPreviewSelectTextColor(ContextCompat.getColor(context, color_white));
        numberSelectMainStyle.setPreviewSelectMarginRight(DensityUtil.dip2px(context, 6));
        numberSelectMainStyle.setSelectBackgroundResources(complete_bg);
        numberSelectMainStyle.setSelectText(send_num);
        numberSelectMainStyle.setSelectTextColor(ContextCompat.getColor(context, color_white));
        numberSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, color_black));
        numberSelectMainStyle.setCompleteSelectRelativeTop(true);
        numberSelectMainStyle.setPreviewSelectRelativeBottom(true);
        numberSelectMainStyle.setAdapterItemIncludeEdge(false);

        // 头部TitleBar 风格
        TitleBarStyle numberTitleBarStyle = new TitleBarStyle();
        numberTitleBarStyle.setHideCancelButton(true);
        numberTitleBarStyle.setAlbumTitleRelativeLeft(true);
        numberTitleBarStyle.setTitleAlbumBackgroundResource(album_bg);
        numberTitleBarStyle.setTitleDrawableRightResource(grey_arrow);
        numberTitleBarStyle.setPreviewTitleLeftBackResource(normal_back);

        // 底部NavBar 风格
        BottomNavBarStyle numberBottomNavBarStyle = new BottomNavBarStyle();
        numberBottomNavBarStyle.setBottomPreviewNarBarBackgroundColor(ContextCompat.getColor(context, color_half_grey));
        numberBottomNavBarStyle.setBottomPreviewNormalText(preview);
        numberBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, color_9b));
        numberBottomNavBarStyle.setBottomPreviewNormalTextSize(16);
        numberBottomNavBarStyle.setCompleteCountTips(false);
        numberBottomNavBarStyle.setBottomPreviewSelectText(preview_num);
        numberBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, color_white));


        selectorStyle.setTitleBarStyle(numberTitleBarStyle);
        selectorStyle.setBottomBarStyle(numberBottomNavBarStyle);
        selectorStyle.setSelectMainStyle(numberSelectMainStyle);
    }

    /**
     * 相册列表上弹显示动画
     */
    public static void setSelectStyle() {
        Context context = cordova.getContext();
        int anim_up_in = resource.getIdentifier("ps_anim_up_in", "anim", pkgName);
        int anim_down_out = resource.getIdentifier("ps_anim_down_out", "anim", pkgName);
        PictureWindowAnimationStyle animationStyle = new PictureWindowAnimationStyle();
        animationStyle.setActivityEnterAnimation(anim_up_in);
        animationStyle.setActivityExitAnimation(anim_down_out);
        selectorStyle.setWindowAnimationStyle(animationStyle);
    }

    /**
     * 是否开启压缩
     *
     * @return
     */
    private ImageFileCompressEngine getCompressFileEngine(Boolean isCompress) {
        return isCompress ? new ImageFileCompressEngine() : null;
    }

    /**
     * 自定义压缩
     */
    private static class ImageFileCompressEngine implements CompressFileEngine {

        @Override
        public void onStartCompress(Context context, ArrayList<Uri> source, OnKeyValueResultCallbackListener call) {
            Luban.with(context).load(source).ignoreBy(100).setRenameListener(new OnRenameListener() {
                @Override
                public String rename(String filePath) {
                    int indexOf = filePath.lastIndexOf(".");
                    String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
                    return DateUtils.getCreateFileName("CMP_") + postfix;
                }
            }).filter(new CompressionPredicate() {
                @Override
                public boolean apply(String path) {
                    if (PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)) {
                        return true;
                    }
                    return !PictureMimeType.isUrlHasGif(path);
                }
            }).setCompressListener(new OnNewCompressListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(String source, File compressFile) {
                   if (call != null) {
                       call.onCallback(source, compressFile.getAbsolutePath());
                   }
                }

                @Override
                public void onError(String source, Throwable e) {
                   if (call != null) {
                       call.onCallback(source, null);
                   }
                }
            }).launch();
        }
    }

    /**
     * 是否开启权限说明弹窗
     *
     * @return
     */
    private OnPermissionDescriptionListener getPermissionDescriptionListener(Boolean isPermission) {
        return isPermission ? new MeOnPermissionDescriptionListener() : null;
    }

    /**
     * 打开/关闭权限说明弹窗
     */
    private static class MeOnPermissionDescriptionListener implements OnPermissionDescriptionListener {

        @Override
        public void onPermissionDescription(Fragment fragment, String[] permissionArray) {
            View rootView = fragment.requireView();
            if (rootView instanceof ViewGroup) {
                addPermissionDescription(false, (ViewGroup) rootView, permissionArray);
            }
        }

        @Override
        public void onDismiss(Fragment fragment) {
            removePermissionDescription((ViewGroup) fragment.requireView());
        }
    }

    /**
     * 添加权限说明弹窗
     *
     * @param viewGroup
     * @param permissionArray
     */
    private static void addPermissionDescription(boolean isHasSimpleXCamera, ViewGroup viewGroup, String[] permissionArray) {
        int dp10 = DensityUtil.dip2px(viewGroup.getContext(), 10);
        int dp15 = DensityUtil.dip2px(viewGroup.getContext(), 15);
        MediumBoldTextView view = new MediumBoldTextView(viewGroup.getContext());
        view.setTag(TAG_EXPLAIN_VIEW);
        view.setTextSize(14);
        view.setTextColor(Color.parseColor("#333333"));
        view.setPadding(dp10, dp15, dp10, dp15);

        String title;
        String explain;

        if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
            title = "相机权限使用说明";
            explain = "相机权限使用说明\n用户app用于拍照/录视频";
        } else {
            title = "存储权限使用说明";
            explain = "存储权限使用说明\n用户app写入/下载/保存/读取/修改/删除图片、视频、文件等信息";
        }
        int startIndex = 0;
        int endOf = startIndex + title.length();
        SpannableStringBuilder builder = new SpannableStringBuilder(explain);
        builder.setSpan(new AbsoluteSizeSpan(DensityUtil.dip2px(viewGroup.getContext(), 16)), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(0xFF333333), startIndex, endOf, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        view.setText(builder);
        int permission_desc_bg = resource.getIdentifier("ps_permission_desc_bg", "drawable", pkgName);
        view.setBackground(ContextCompat.getDrawable(viewGroup.getContext(), permission_desc_bg));
        ConstraintLayout.LayoutParams layoutParams =
                new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topToBottom = ConstraintSet.PARENT_ID;
        layoutParams.leftToLeft = ConstraintSet.PARENT_ID;
        layoutParams.leftMargin = dp10;
        layoutParams.rightMargin = dp10;
        viewGroup.addView(view, layoutParams);
    }

    /**
     * 移除权限说明弹窗
     *
     * @param viewGroup
     */
    private static void removePermissionDescription(ViewGroup viewGroup) {
        View tagExplainView = viewGroup.findViewWithTag(TAG_EXPLAIN_VIEW);
        viewGroup.removeView(tagExplainView);
    }

    /**
     * 是否开启权限拒绝后回调
     *
     * @return
     */
    private OnPermissionDeniedListener getPermissionDeniedListener(Boolean isPermission) {
        return isPermission ? new MeOnPermissionDeniedListener() : null;
    }

    /**
     * 权限拒绝后回调
     */
    private static class MeOnPermissionDeniedListener implements OnPermissionDeniedListener {

        @Override
        public void onDenied(Fragment fragment, String[] permissionArray,
                             int requestCode, OnCallbackListener<Boolean> call) {
            String tips;
            if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
                tips = "缺少相机权限\n可能会导致不能使用摄像头功能";
            } else {
                tips = "缺少存储权限\n访问您设备上的照片、媒体内容和文件";
            }
            RemindDialog dialog = RemindDialog.buildDialog(fragment.getContext(), tips);
            dialog.setButtonText("去设置");
            dialog.setButtonTextColor(0xFF7D7DFF);
            dialog.setContentTextColor(0xFF333333);
            dialog.setOnDialogClickListener(new RemindDialog.OnDialogClickListener() {
                @Override
                public void onClick(View view) {
                    PermissionUtil.goIntentSetting(fragment, requestCode);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
}
