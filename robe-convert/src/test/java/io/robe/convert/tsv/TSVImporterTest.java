package io.robe.convert.tsv;

import io.robe.convert.SamplePojo;
import io.robe.convert.TestData;
import org.junit.Test;

import java.util.List;

public class TSVImporterTest {
    @Test
    public void testImportStream() throws Exception {
        TSVImporter importer = new TSVImporter(SamplePojo.class);
        List<SamplePojo> list = importer.importStream(TSVImporterTest.class.getClassLoader().getResourceAsStream("sample.tsv"));

        assert list.size() == TestData.getData().size();

        int index = 0;
        for (SamplePojo item : list) {
            SamplePojo ref = TestData.getData().get(index++);
            assert item.equals(ref);
            System.out.println(ref);
        }
    }

}
