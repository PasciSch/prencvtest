package opencvtests;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.*;
import org.opencv.imgproc.*;
import org.opencv.photo.Photo;

public class PictureTest extends JPanel{

    BufferedImage image;
    static JFrame frame0;

    public static void main (String args[]) throws InterruptedException{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        DisplayHelper display = new DisplayHelper();
        
        Mat img_object = Imgcodecs.imread("resources/view.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img_scene = Imgcodecs.imread("resources/map.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        
        int minHessian = 400;
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
        MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();
        
        // Keypoints detection
        detector.detect(img_object, keypoints_object);
        detector.detect(img_scene, keypoints_scene);
        
        // Calculate descriptors
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        Mat descriptor_object = new Mat();
        Mat descriptor_scene = new Mat();
        
        extractor.compute(img_object, keypoints_object, descriptor_object);
        extractor.compute(img_scene, keypoints_scene, descriptor_scene);
        
        // Match descriptors using FLANN matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptor_object, descriptor_scene, matches);
        
        
        // Max and min distances
        double max_dist = 0, min_dist = 100;
        for (int i = 0; i < descriptor_object.rows(); i++)
        {
            double dist = matches.toArray()[i].distance;
            if(dist < min_dist) min_dist = dist;
            if(dist > max_dist) max_dist = dist;
        }
        
        System.out.printf("-- Max dsit : %f", max_dist);
        System.out.printf("-- Min dsit : %f", min_dist);
        
        // Select only 'good' matches ( < 3*min_dist )
        List<DMatch> good_matches = new ArrayList<DMatch>();
        final DMatch[] matchesArray = matches.toArray();
        for (int i = 0; i < descriptor_object.rows(); i++)
        {
            if (matchesArray[i].distance < 3 * min_dist)
            {
                good_matches.add(matchesArray[i]);
            }
        }
        
        Mat img_matches = new Mat();
        MatOfDMatch good_matches_mat = new MatOfDMatch();
        good_matches_mat.fromList(good_matches);
        Features2d.drawMatches(img_object, keypoints_object, img_scene, keypoints_scene, good_matches_mat, img_matches);
        
        BufferedImage image = display.MatToBufferedImage(img_matches);
        display.window(image, "bla", 0, 0);
    }
}