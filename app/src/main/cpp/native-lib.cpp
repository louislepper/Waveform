#include <jni.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define TAG "NativeLib"

using namespace std;
using namespace cv;

extern "C" {
void JNICALL
Java_com_louislepper_waveform_MainActivity_adaptiveThresholdFromJNI(JNIEnv *env,
                                                                                   jobject instance,
                                                                                   jlong matAddr) {

    // get Mat from raw address
    Mat &mat = *(Mat *) matAddr;

    clock_t begin = clock();

    // Gaussian blur
    cv::GaussianBlur(mat, mat, cv::Size(5, 5), 2, 2);

    // Thresholding
    const int LIGHT_THRESH = 160;
    cv::threshold(mat, mat, LIGHT_THRESH, LIGHT_THRESH, cv::THRESH_TRUNC);

    // More Gaussian blur
    cv::GaussianBlur(mat, mat, cv::Size(5, 5), 2, 2);
    cv::GaussianBlur(mat, mat, cv::Size(9, 9), 0, 0);

    // Morphological dilation
    cv::Mat kernel = cv::getStructuringElement(cv::MORPH_ELLIPSE, cv::Size(5, 5));
    cv::dilate(mat, mat, kernel);

    // Canny edge detection
    const double CANNY_LOW = 10.0;
    const double CANNY_HIGH = 30.0;
    cv::Canny(mat, mat, CANNY_LOW, CANNY_HIGH);

    // I wonder if all this could be replaced with
    //cv::adaptiveThreshold(mat, mat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 21, 5);

    // log computation time to Android Logcat
    double totalTime = double(clock() - begin) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, TAG, "adaptiveThreshold computation time = %f seconds\n",
                        totalTime);
}
}