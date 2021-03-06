/**
 * Copyright 2011 AnyChart.Com Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anychart.gjstags.ctags;

import com.anychart.gjstags.CommandLineRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Collections;
import java.util.List;

/**
 * @author Aleksandr Batsuev (alex@batsuev.com)
 */
public class CTagsFile {

    private static final String CTAGS_HEADER =
            "!_TAG_FILE_FORMAT\t2\t/extended format; --format=1 will not append ;\" to lines/\n" +
                    "!_TAG_FILE_SORTED\t1\t/0=unsorted, 1=sorted, 2=foldcase/\n" +
                    "!_TAG_PROGRAM_AUTHOR\tAnyChart.Com Team\n" +
                    "!_TAG_PROGRAM_NAME\tgjstags\t//\n" +
                    "!_TAG_PROGRAM_URL\thttps://github.com/AnyChart/gjstags\t/official site/\n" +
                    "!_TAG_PROGRAM_VERSION\t"+ CommandLineRunner.VERSION+"\t//\n";

    private List<CTag> entries;

    public CTagsFile() {
        this.entries = new ArrayList<CTag>();
    }

    public void update(CTag[] newEntries) {
        //remove all old entries with specific file names
        for (CTag tag : newEntries) {
            String fileName = tag.getFile();
            for (CTag currentTag : this.entries) {
                if (currentTag.getFile().equals(fileName)) {
                    this.entries.remove(currentTag);
                }
            }
        }

        Collections.addAll(this.entries, newEntries);
    }

    public void writeETags(String fileName) throws IOException {
        BufferedWriter writer =
            new BufferedWriter(new FileWriter(fileName));
        HashMap<String, List<CTag>> byFile = new HashMap<String, List<CTag>>();
        for (CTag tag : this.entries) {
            if (!byFile.containsKey(tag.getFile()))
                byFile.put(tag.getFile(), new ArrayList<CTag>());
            byFile.get(tag.getFile()).add(tag);
        }
        Iterator<Map.Entry<String, List<CTag>>> entries = byFile.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<CTag>> entry = entries.next();
            String file = entry.getKey();
            List<CTag> entriesByFile = entry.getValue();
            writer.write((char)0x0c);
            writer.write('\n');
            writer.write(file);
            writer.write(',');
            writer.write('\n');
            for (CTag tag : entriesByFile) {
                tag.writeETagString(writer);
            }
        }
        writer.close();
    }

    public void writeCTags(String fileName) throws IOException {
        BufferedWriter writer =
            new BufferedWriter(new FileWriter(fileName));
        writer.write(CTAGS_HEADER);
        TreeMap<String, List<CTag>> sorted = new TreeMap<String, List<CTag>>();
        for (CTag tag : this.entries) {
            if (!sorted.containsKey(tag.getSanitizedName()))
                sorted.put(tag.getSanitizedName(), new ArrayList<CTag>());
            sorted.get(tag.getSanitizedName()).add(tag);
        }
        Iterator<Map.Entry<String, List<CTag>>> entries = sorted.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, List<CTag>> entry = entries.next();
            List<CTag> entriesByName = entry.getValue();
            for (CTag tag : entriesByName) {
                tag.writeCTagString(writer);
            }
        }
        writer.close();
    }

    public static CTagsFile fromFile(String fileName) throws IOException {
        CTagsFile file = new CTagsFile();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        while (line != null) {
            CTag tag = CTag.fromString(line);
            if (tag != null)
                file.entries.add(tag);
            line = reader.readLine();
        }
        return file;
    }
}
