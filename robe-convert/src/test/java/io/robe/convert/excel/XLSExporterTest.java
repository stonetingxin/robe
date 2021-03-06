package io.robe.convert.excel;


import io.robe.convert.FileUtil;
import io.robe.convert.SamplePojo;
import io.robe.convert.TestData;
import io.robe.convert.excel.exporter.XLSExporter;
import io.robe.convert.excel.importer.XLSImporter;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class XLSExporterTest {


    @Test
    public void exportStream() throws Exception {

        // Write to temp file
        File outputFile = FileUtil.getRandomTempFile();
        OutputStream outputStream = new FileOutputStream(outputFile);
        XLSExporter<SamplePojo> xlsExporter = new XLSExporter(SamplePojo.class, false);
        xlsExporter.exportStream(outputStream, TestData.getData().iterator());
        outputStream.flush();
        outputStream.close();

        XLSImporter<SamplePojo> xlsImporter = new XLSImporter(SamplePojo.class, false);
        List<SamplePojo> list = xlsImporter.importStream(new FileInputStream(outputFile.getPath()));
        assert list.size() == TestData.getData().size();
        int index = 0;

        for (SamplePojo item : list) {
            SamplePojo ref = TestData.getData().get(index++);
            assert item.equals(ref);
            System.out.println(ref);
        }

        outputFile.delete();
    }

    @Test
    public void exportStreamWithTitle() throws Exception {
        // Write to temp file
        File outputFile = FileUtil.getRandomTempFile();
        OutputStream outputStream = new FileOutputStream(outputFile);
        XLSExporter<SamplePojo> xlsExporter = new XLSExporter(SamplePojo.class, true);
        xlsExporter.exportStream(outputStream, TestData.getData().iterator());
        outputStream.flush();
        outputStream.close();

        XLSImporter<SamplePojo> xlsImporter = new XLSImporter(SamplePojo.class, true);
        List<SamplePojo> list = xlsImporter.importStream(new FileInputStream(outputFile.getPath()));
        assert list.size() == TestData.getData().size();
        int index = 0;

        for (SamplePojo item : list) {
            SamplePojo ref = TestData.getData().get(index++);
            assert item.equals(ref);
            System.out.println(ref);
        }

        outputFile.delete();

    }
}
