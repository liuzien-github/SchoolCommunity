package cn.mine.community.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SensitiveWordsFilter {
    private class TrieNode {
        private boolean isKeywordEnd = false;
        private Map<Character, TrieNode> map = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addTrieNode(Character character) {
            if (!map.containsKey(character)) {
                map.put(character, new TrieNode());
            }
        }

        public void clearTrieNode(Character character) {
            map.remove(character);
        }

        public TrieNode getTrieNode(Character character) {
            return map.get(character);
        }
    }
    private static final String REPLACEMENT = "*";
    private TrieNode root = new TrieNode();

    @PostConstruct
    private void init() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String keyWord = null;
            while ((keyWord = bufferedReader.readLine()) != null) {
                addKeyWord(keyWord);
            }
        } catch (IOException e) {
            log.error("敏感词文件加载失败：" + e.getMessage());
        }
    }

    private void addKeyWord(String keyWord) {
        TrieNode temp = root;
        int len = keyWord.length();
        for (int i = 0; i < len; i++) {
            char ch = keyWord.charAt(i);
            temp.addTrieNode(ch);
            temp = temp.getTrieNode(ch);
        }
        temp.setKeywordEnd(true);
    }

    //如果不是东亚文字和英文字母则跳过
    private boolean isSymbol(char ch) {
        return !CharUtils.isAsciiAlphanumeric(ch) && (ch < 0x2E80 || ch > 0x9FFF);
    }

    public String filter(String text) {
        if (text == null)
            return null;

        // 指针1
        TrieNode tempNode = root;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while(begin < text.length()){
            if(position < text.length()) {
                Character c = text.charAt(position);

                // 跳过符号
                if (isSymbol(c)) {
                    if (tempNode == root) {
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getTrieNode(c);
                if (tempNode == null) {
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = root;
                }
                // 发现敏感词
                else if (tempNode.isKeywordEnd()) {
                    sb.append(REPLACEMENT);
                    begin = ++position;
                    tempNode = root;
                }
                // 检查下一个字符
                else {
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }
        }
        return sb.toString();
    }
}
