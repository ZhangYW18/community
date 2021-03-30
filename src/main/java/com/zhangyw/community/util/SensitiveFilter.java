package com.zhangyw.community.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

// A filter leveraging Aho–Corasick algorithm.
@Component
public class SensitiveFilter {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // Replace sensitive words by replacement
    private static final char REPLACEMENT = '*';

    // 关于静态内部类：如果你在这个类里面需要外面类的引用，就不要用static。反之就尽量用static，这样可以提高性能。
    // https://www.zhihu.com/question/28197253
    private static class Node{
        private int wordLen = 0;
        private Node fail;
        private Map<Character, Node> subNodes = new HashMap<>();
    }
    private Node root = new Node();

    // 创建对象之后初始化
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addSensitiveWord(keyword);
            }
            this.buildFail();
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }
    }

    // Add a word on Trie.
    private void addWordOnTrie(Node nowNode, String word, int index) {
        if (index==word.length()) {
            nowNode.wordLen = index;
            return;
        }
        if (nowNode.subNodes.get(word.charAt(index))==null) {
            nowNode.subNodes.put(word.charAt(index), new Node());
        }
        addWordOnTrie(nowNode.subNodes.get(word.charAt(index)), word, index+1);
    }

    private void addSensitiveWord(String word) {
        addWordOnTrie(this.root, word, 0);
    }

    // Build the fail field according to Aho-Corasick algorithm.
    private void buildFail() {
        this.root.fail = root;
        Queue<Node> q = new LinkedList<Node>();
        for (Map.Entry<Character, Node> entry: root.subNodes.entrySet()) {
            entry.getValue().fail = root;
            q.offer(entry.getValue());
        }
        while (!q.isEmpty()) {
            Node tmp = q.poll();
            for (Map.Entry<Character, Node> entry: tmp.subNodes.entrySet()) {
                Node subNode = entry.getValue();
                subNode.fail = tmp.fail;
                q.offer(subNode);
                while (subNode.fail!=this.root && !subNode.fail.subNodes.containsKey(entry.getKey()))
                    subNode.fail = subNode.fail.fail;
                if (subNode.fail.subNodes.containsKey(entry.getKey()))
                    subNode.fail = subNode.fail.subNodes.get(entry.getKey());
            }
        }
    }

    private String searchAndFilter(String s) {
        int n = s.length();
        Node node=this.root;
        StringBuilder filteredStr = new StringBuilder();
        for (int i=0;i<n;i++) {
            filteredStr.append(s.charAt(i));
            while (node!=this.root && !node.subNodes.containsKey(s.charAt(i))) node=node.fail;
            if (!node.subNodes.containsKey(s.charAt(i))) continue;
            node = node.subNodes.get(s.charAt(i));
            int maxMatchedStrLen = 0;
            Node tmp = node;
            while (tmp!=this.root && tmp.wordLen>0) {
                maxMatchedStrLen = Math.max(maxMatchedStrLen, tmp.wordLen);
                tmp = tmp.fail;
            }
            filteredStr.replace(i-maxMatchedStrLen+1, i+1, StringUtils.repeat(REPLACEMENT, maxMatchedStrLen));
        }
        return filteredStr.toString();
    }

    /**
     * Replace all sensitive words in text by REPLACEMENT.
     * @param text
     * @return
     */
    public String filter(String text) {
        return searchAndFilter(text);
    }
}
