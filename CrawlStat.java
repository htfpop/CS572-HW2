import java.util.List;
import java.util.Stack;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CrawlStat {
    private int totalProcessedPages;
    private int totalFetchedPages;
    private int totalSuccesses;
    private int totalFailedOrAborted;
    private long totalLinks;
    private long totalTextSize;
    public final Stack<String> stack_csv1;
    public final Stack<String> stack_csv2;
    public final Stack<String> stack_csv3;
    public CrawlStat()
    {
        this.totalFetchedPages = 0;
        this.totalProcessedPages = 0;
        this.totalSuccesses = 0;
        this.totalFailedOrAborted = 0;
        this.totalLinks = 0;
        this.totalTextSize = 0;
        this.stack_csv1 = new Stack<>();
        this.stack_csv2 = new Stack<>();
        this.stack_csv3 = new Stack<>();
    }

    public void stackPush(Stack<String> stack, String s)
    {
        stack.push(s);
    }

    public String stackPop(Stack<String> stack)
    {
        return stack.pop();
    }

    public int stackSize(Stack<String> stack)
    {
        return stack.size();
    }


    public int getTotalProcessedPages() {
        return totalProcessedPages;
    }

    public void setTotalProcessedPages(int totalProcessedPages) {
        this.totalProcessedPages = totalProcessedPages;
    }

    public void incProcessedPages() {
        this.totalProcessedPages++;
    }

    public void incTotalSuccess() {
        this.totalSuccesses++;
    }
    public int getTotalSuccess() {
        return this.totalSuccesses;
    }
    public void incTotalFailedOrAborted() {
        this.totalFailedOrAborted++;
    }

    public int getTotalFailedOrAborted() {return this.totalFailedOrAborted;}

    public void incFetchedPages() {
        this.totalFetchedPages++;
    }

    public int getFetchedPages() {
        return this.totalFetchedPages;
    }

    public long getTotalLinks() {
        return totalLinks;
    }

    public void setTotalLinks(long totalLinks) {
        this.totalLinks = totalLinks;
    }

    public long getTotalTextSize() {
        return totalTextSize;
    }

    public void setTotalTextSize(long totalTextSize) {
        this.totalTextSize = totalTextSize;
    }

    public void incTotalLinks(int count) {
        this.totalLinks += count;
    }

    public void incTotalTextSize(int count) {
        this.totalTextSize += count;
    }
}