package org.freedesktop.gstreamer.meta;

import org.freedesktop.gstreamer.Buffer;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.SampleTester;
import org.freedesktop.gstreamer.Version;
import org.freedesktop.gstreamer.timecode.VideoTimeCode;
import org.freedesktop.gstreamer.timecode.VideoTimeCodeConfig;
import org.freedesktop.gstreamer.util.TestAssumptions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.freedesktop.gstreamer.timecode.VideoTimeCodeFlags.GST_VIDEO_TIME_CODE_FLAGS_DROP_FRAME;
import static org.freedesktop.gstreamer.timecode.VideoTimeCodeFlags.GST_VIDEO_TIME_CODE_FLAGS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * Copyright (c) 2020 Petr Lastovka
 *
 * This file is part of gstreamer-java.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
public class GstTimeCodeMetaTest {

    @BeforeClass
    public static void beforeClass() {
        Gst.init();
    }

    @AfterClass
    public static void afterClass() {
        Gst.deinit();
    }

    @Test
    public void testVideoWithoutTimeCodeMeta() {
        // method containsMetadata is available since 1.14
        TestAssumptions.requireGstVersion(1, 14);
        SampleTester.test(sample -> {
            Buffer buffer = sample.getBuffer();
            assertFalse("Default video not contains timecode metadata", buffer.containsMetadata(VideoTimeCodeMeta.class));
        }, "videotestsrc do-timestamp=true ! x264enc  ! mxfmux ! decodebin ! appsink name=myappsink");
    }

    @Test
    public void testVideoTimeCodeMetaPal() {
        // timecodestamper is available since 1.10
        TestAssumptions.requireGstVersion(1,10);
        SampleTester.test(sample -> {
            Buffer buffer = sample.getBuffer();
            if (Gst.testVersion(1, 14)) {
                assertTrue("Video should contains timecode meta", buffer.containsMetadata(VideoTimeCodeMeta.class));
            }
            VideoTimeCodeMeta meta = buffer.getMetadata(VideoTimeCodeMeta.class);
            assertNotNull(meta);
            VideoTimeCode timeCode = meta.getTimeCode();

            //first frame 00:00:00:00
            assertEquals(0, timeCode.getHours());
            assertEquals(0, timeCode.getMinutes());
            assertEquals(0, timeCode.getSeconds());
            assertEquals(0, timeCode.getFrames());

            VideoTimeCodeConfig timeCodeConfig = timeCode.getTCConfig();
            // PAL standard 25/1
            assertEquals(25, timeCodeConfig.getFramerateNumerator());
            assertEquals(1, timeCodeConfig.getFramerateDenominator());
            assertEquals(GST_VIDEO_TIME_CODE_FLAGS_NONE, timeCodeConfig.getTimeCodeFlags());

        }, "videotestsrc ! video/x-raw,framerate=25/1 ! timecodestamper ! videoconvert ! appsink name=myappsink");
    }

    @Test
    public void testVideoTimeCodeNTSCDrop() {
        // timecodestamper is available since 1.10
        TestAssumptions.requireGstVersion(1, 10);
        SampleTester.test(sample -> {
            Buffer buffer = sample.getBuffer();
            if (Gst.testVersion(1, 14)) {
                assertTrue("Video should contains timecode meta", buffer.containsMetadata(VideoTimeCodeMeta.class));
            }
            VideoTimeCodeMeta meta = buffer.getMetadata(VideoTimeCodeMeta.class);
            assertNotNull(meta);
            VideoTimeCode timeCode = meta.getTimeCode();

            //first frame 00:00:00;00
            assertEquals(0, timeCode.getHours());
            assertEquals(0, timeCode.getMinutes());
            assertEquals(0, timeCode.getSeconds());
            assertEquals(0, timeCode.getFrames());

            VideoTimeCodeConfig timeCodeConfig = timeCode.getTCConfig();
            // NTSC drop standard 30000/1001
            assertEquals(30000, timeCodeConfig.getFramerateNumerator());
            assertEquals(1001, timeCodeConfig.getFramerateDenominator());
            assertEquals(GST_VIDEO_TIME_CODE_FLAGS_DROP_FRAME, timeCodeConfig.getTimeCodeFlags());

        }, "videotestsrc ! video/x-raw,framerate=30000/1001 ! timecodestamper drop-frame=true ! videoconvert ! appsink name=myappsink");
    }

    /**
     * Handle last frame of first minute
     */
    @Test
    public void testVideoTimeCodeNTSCDropFrame() {
        // timecodestamper is available since 1.10
        TestAssumptions.requireGstVersion(1, 10);
        SampleTester.test(sample -> {
            Buffer buffer = sample.getBuffer();
            if (Gst.testVersion(1, 14)) {
                assertTrue("Video should contains timecode meta", buffer.containsMetadata(VideoTimeCodeMeta.class));
            }
            VideoTimeCodeMeta meta = buffer.getMetadata(VideoTimeCodeMeta.class);
            assertNotNull(meta);
            VideoTimeCode timeCode = meta.getTimeCode();

            //first frame 00:00:00;29
            assertEquals(0, timeCode.getHours());
            assertEquals(0, timeCode.getMinutes());
            assertEquals(0, timeCode.getSeconds());
            assertEquals(29, timeCode.getFrames());

            VideoTimeCodeConfig timeCodeConfig = timeCode.getTCConfig();
            // NTSC drop standard 30000/1001
            assertEquals(30000, timeCodeConfig.getFramerateNumerator());
            assertEquals(1001, timeCodeConfig.getFramerateDenominator());
            assertEquals(GST_VIDEO_TIME_CODE_FLAGS_DROP_FRAME, timeCodeConfig.getTimeCodeFlags());
        }, "videotestsrc ! video/x-raw,framerate=30000/1001 ! videoconvert ! timecodestamper drop-frame=true ! videoconvert ! appsink name=myappsink", 29);
    }

    @Test
    public void testVideoTimeCodeNTSCNonDrop() {
        // timecodestamper is available since 1.10
        TestAssumptions.requireGstVersion(1, 10);
        SampleTester.test(sample -> {
            Buffer buffer = sample.getBuffer();
            if (Gst.testVersion(1, 14)) {
                assertTrue("Video should contains timecode meta", buffer.containsMetadata(VideoTimeCodeMeta.class));
            }
            VideoTimeCodeMeta meta = buffer.getMetadata(VideoTimeCodeMeta.class);
            assertNotNull(meta);
            VideoTimeCode timeCode = meta.getTimeCode();

            //first frame 00:00:00:00
            assertEquals(0, timeCode.getHours());
            assertEquals(0, timeCode.getMinutes());
            assertEquals(0, timeCode.getSeconds());
            assertEquals(0, timeCode.getFrames());

            VideoTimeCodeConfig timeCodeConfig = timeCode.getTCConfig();
            // NTSC drop standard 30/1
            assertEquals(30, timeCodeConfig.getFramerateNumerator());
            assertEquals(1, timeCodeConfig.getFramerateDenominator());
            assertEquals(GST_VIDEO_TIME_CODE_FLAGS_NONE, timeCodeConfig.getTimeCodeFlags());

        }, "videotestsrc ! video/x-raw,framerate=30/1 ! timecodestamper ! videoconvert ! appsink name=myappsink");
    }
}
