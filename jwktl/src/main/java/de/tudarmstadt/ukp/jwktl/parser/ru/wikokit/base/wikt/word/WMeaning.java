/*******************************************************************************
 * Copyright 2008 Andrew Krizhanovsky <andrew.krizhanovsky at gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.word;

import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikipedia.language.LanguageType;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.constant.ContextLabel;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.multi.en.WMeaningEn;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.multi.ru.WMeaningRu;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.util.POSText;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.util.WikiText;
import de.tudarmstadt.ukp.jwktl.parser.ru.wikokit.base.wikt.util.WikiWord;

/** Meaning consists of <PRE>
 * # Definition (preceded by "#", which causes automatic numbering).
 * #* and Quotations.      </PRE>
 */
public class WMeaning {

    // StringBuffer definition;
    // + wiki word, + number of wiki word or number of first char of wikiword in definition
    
    /** Contexual information for definitions, e.g. "idiom" from text "# {{idiom}} [[bullet]]s" */
    private ContextLabel[]  labels;

    private WikiText definition;

    /** True, if the definition defines inflection of the word with the help of
     * (1) template {{form of|}}, or {{plural of|}}, 
     * (2) strictly defined phrase (e.g. "Plural form of")
     */
    private boolean  form_of_inflection;

    /** Word definition, e.g. "bullets" from text "# {{idiom}} [[bullet]]s" */
    //private StringBuffer    definition;

    /** Wiki internal links, e.g. "bullet" from text "# {{idiom}} [[bullet]]s" */
    //private WikiWord[] wiki_words;
    
    /** Example sentences and quotations. */
    private WQuote[] quote;

    private final static WQuote[] NULL_WQUOTE_ARRAY = new WQuote[0];
    private final static WMeaning[] NULL_WMEANING_ARRAY = new WMeaning[0];
    private final static WMeaning   NULL_WMEANING       = new WMeaning();

    public WMeaning() {
        labels = null;
        definition = null;
        quote = null;
        form_of_inflection = false;
    }

        /** Frees memory recursively. */
    public void free ()
    {
        labels = null;

        if(null != quote) {
            //for(WQuote q : quote)
                //q.free();
            quote = null;
        }
    }

    /** Constructor.
     *
     * @param page_title
     * @param _labels
     * @param _definition wikified text of the definition
     * @param _quote could be null
     * @param _template_not_def true if there is template (e.g. {{form of|}} or
     * {{plural of|}}) instead of definiton text (in enwikt)
     */
    public WMeaning(String page_title,ContextLabel[] _labels,
                    String _definition, WQuote[] _quote, boolean _template_not_def) {
        labels = _labels;
        wikified_text = _definition;
        definition = WikiText.createOnePhrase(page_title, _definition);
        
        form_of_inflection = _template_not_def;

        if(null == _quote)
            quote = NULL_WQUOTE_ARRAY;
        else
            quote = _quote;

    }

    
    /** True if the definition defines inflection of the word with the help of
     * (1) the template (e.g. {{form of|}} or {{plural of|}}), or
     * (2) strictly defined phrase (e.g. "Plural form of")
     * instead of the usual definiton text (in enwikt). */
    public boolean isFormOfInflection() {
        return form_of_inflection;
    }

    /** Gets array of context labels in the definition. */
    public ContextLabel[] getLabels() {
        return labels;
    }

    /** Gets definition line of text. */
    public String getDefinition() {
        return definition.getVisibleText();
    }
    
    /** Gets array of internal links in the definition (wiki words, i.e. words with hyperlinks). */
    public WikiWord[] getWikiWords() {
        return definition.getWikiWords();
    }

    /** Gets wiki_text. */
    public WikiText getWikiText() {
        return definition;
    }
    
    /** Gets array of quotes (sentences) from the definition. */
    public WQuote[] getQuotes() {
        return quote;
    }

    /** Parses text (related to the POS), creates and fills array of meanings (WMeaning).
     * @param wikt_lang     language of Wiktionary
     * @param page_title    word which are described in this article 'text'
     * @param lang_section  language of this section of an article
     * @param pt            POSText defines POS stored in pt.text
     * @return
     */
    public static WMeaning[] parse (
                    LanguageType wikt_lang,
                    String page_title,
                    LanguageType lang_section,
                    POSText pt)
    {
        // === Level III. Meaning ===
        WMeaning[] wm = NULL_WMEANING_ARRAY;

        LanguageType l = wikt_lang;

        if(l == LanguageType.ru) {

            // get context labels, definitions, and quotations... todo
            /*   if(0==wm.length) {
                    return NULL_WMEANING_ARRAY;
            }*/
          wm = WMeaningRu.parse(page_title, lang_section, pt);

        } else if(l == LanguageType.en) {
            wm = WMeaningEn.parse(page_title, lang_section, pt);

        //} //else if(code.equalsIgnoreCase( "simple" )) {
          //  return WordSimple;

            // todo
            // ...

        } else {
            throw new NullPointerException("Null LanguageType");
        }
        
        return wm;
    }

    /** Parses one definition (one line in Russian, several lines in English 
     * Wiktionary), i.e. extracts {{label.}}, definition,
     * {{example|Quotation sentence.}}, creates and fills a meaning (WMeaning).
     * @param wikt_lang     language of Wiktionary
     * @param page_title    word which are described in this article 'text'
     * @param lang_section  language of this section of an article
     * @param def_text      text of one definition
     * @return WMeaning or null if the line is not started from "#"
     */
    public static WMeaning parseOneDefinition(LanguageType wikt_lang,
                    String page_title,
                    LanguageType lang_section,
                    String def_text)
    {
        WMeaning wm = NULL_WMEANING;

        LanguageType l = wikt_lang;

        if(l == LanguageType.ru) {
            wm = WMeaningRu.parseOneDefinition(page_title, lang_section, def_text);

        } else if(l == LanguageType.en) {
            wm = WMeaningEn.parseOneDefinition(page_title, lang_section, def_text);

          //  return WordEn;
        //} //else if(code.equalsIgnoreCase( "simple" )) {
          //  return WordSimple;

            // todo
            // ...

        } else {
            throw new NullPointerException("Null LanguageType");
        }

        return wm;
    }
    
    //////////////////////
    /// JWKTL interface
    //////////////////////
    
    /** Meaning text with wiki markup, with context labels. */
    private String wikified_text;
    
    public String getWikifiedText() {
   	return wikified_text;
    }

}
