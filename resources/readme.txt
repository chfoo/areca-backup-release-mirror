If you want to add a new language :
1) Make a copy of the english translation ("translations/resources_en.properties" file), name it after the language you want to add ("de" for german, "it" for italian, ...)
2) Edit the language list ("translations/languages.txt" file), and add the language you just translated

VERY IMPORTANT : The content of the translations file MUST be encoded using the ISO-8859-1 encoding. 
Non-compatible characters (for instance chinese characters) must be unicode-encoded (using a \u escape character). 

It is strongly advised to use a translation tool in order to ensure translation files consistency. 
For instance : 
- "Zaval Java Resource Editor", available at http://www.zaval.org/products/jrc-editor/download/index.html.
- "Eclipse ResourceBundle Editor Plugin", available at http://www.resourcebundleeditor.com/wiki/Download