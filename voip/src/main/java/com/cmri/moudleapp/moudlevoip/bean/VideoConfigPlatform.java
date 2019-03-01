package com.cmri.moudleapp.moudlevoip.bean;

/**
 * Created by zhangcong on 2017/8/30.
 */

public class VideoConfigPlatform {
    private boolean H264EncodeSurface;
    private boolean LocalPreviewMirror;
    private String MANUFACTURER;
    private String MODEL;
    private String PRODUCT;
    private String ProvinceCode;
    private int UVFormat;

    @Override
    public String toString() {
        return "VideoConfigPlatform{" +
                "H264EncodeSurface=" + H264EncodeSurface +
                ", LocalPreviewMirror=" + LocalPreviewMirror +
                ", MANUFACTURER='" + MANUFACTURER + '\'' +
                ", MODEL='" + MODEL + '\'' +
                ", PRODUCT='" + PRODUCT + '\'' +
                ", ProvinceCode='" + ProvinceCode + '\'' +
                ", UVFormat=" + UVFormat +
                ", VideoDecoderType=" + VideoDecoderType +
                ", VideoEncoderType=" + VideoEncoderType +
                ", VideoResolutionType=" + VideoResolutionType +
                ", VideoSupport=" + VideoSupport +
                ", YUVFormat=" + YUVFormat +
                '}';
    }

    private int VideoDecoderType;
    private int VideoEncoderType;
    private int VideoResolutionType;
    private boolean VideoSupport;
    private int YUVFormat;

    public boolean isH264EncodeSurface() {
        return H264EncodeSurface;
    }

    public void setH264EncodeSurface(boolean h264EncodeSurface) {
        H264EncodeSurface = h264EncodeSurface;
    }

    public boolean isLocalPreviewMirror() {
        return LocalPreviewMirror;
    }

    public void setLocalPreviewMirror(boolean localPreviewMirror) {
        LocalPreviewMirror = localPreviewMirror;
    }

    public String getMANUFACTURER() {
        return MANUFACTURER;
    }

    public void setMANUFACTURER(String MANUFACTURER) {
        this.MANUFACTURER = MANUFACTURER;
    }

    public String getMODEL() {
        return MODEL;
    }

    public void setMODEL(String MODEL) {
        this.MODEL = MODEL;
    }

    public String getPRODUCT() {
        return PRODUCT;
    }

    public void setPRODUCT(String PRODUCT) {
        this.PRODUCT = PRODUCT;
    }

    public String getProvinceCode() {
        return ProvinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        ProvinceCode = provinceCode;
    }

    public int getUVFormat() {
        return UVFormat;
    }

    public void setUVFormat(int UVFormat) {
        this.UVFormat = UVFormat;
    }

    public int getVideoDecoderType() {
        return VideoDecoderType;
    }

    public void setVideoDecoderType(int videoDecoderType) {
        VideoDecoderType = videoDecoderType;
    }

    public int getVideoEncoderType() {
        return VideoEncoderType;
    }

    public void setVideoEncoderType(int videoEncoderType) {
        VideoEncoderType = videoEncoderType;
    }

    public int getVideoResolutionType() {
        return VideoResolutionType;
    }

    public void setVideoResolutionType(int videoResolutionType) {
        VideoResolutionType = videoResolutionType;
    }

    public boolean isVideoSupport() {
        return VideoSupport;
    }

    public void setVideoSupport(boolean videoSupport) {
        VideoSupport = videoSupport;
    }

    public int getYUVFormat() {
        return YUVFormat;
    }

    public void setYUVFormat(int YUVFormat) {
        this.YUVFormat = YUVFormat;
    }

}
