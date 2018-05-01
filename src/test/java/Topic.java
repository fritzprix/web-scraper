import io.reactivex.Observable;

import java.util.List;

public enum Topic {
    GOLD("gold", "price", "invest"),
    WAR("war", "", "nuclear", "terrorist attack", "combat"),
    CRISIS("crisis", "climate", "global warming", "sea level", "environmental"),
    STOCK("stock","market","price", "nasdaq", "market"),
    SAMSUNG_ELECTRONICS("samsung", "", "electronics", "new product", "new flagship", "semiconductor", ""),
    GOOGLE("google", "", "alphabet", "new"),
    APPLE("apple", "", "product", "iphone", "apple mac", "new"),
    AMAZON("amazon", "", "amazon web", "echo", "echo dot"),
    NVIDA("nvidia", "", "GPU", "cloud", "machine learning", "AI", "artificial intelligence"),
    BLOCKCHAIN("blockchain", "", "scam"),
    CRYPTOCURRENCY_NEUTRAL("crypto", "", "currency", "money"),
    CRYPTOCURRENCY_NEG("crypto","scam", "bear", "down", "fall", "regulation"),
    CRYPTOCURRENCY_POS("crypto business", "adopt", "ICO", "bull", "rise", "upside", "surge"),
    BITCOIN_NEG("bitcoin", "downside", "bear", "ban", "tax", "drop", "regulation","ponzi"),
    BITCOIN_POS("bitcoin","upside", "new", "surge", "adopt", "new application"),
    BITCOIN_NEUTRAL("bitcoin", "","price","invest"),
    ETHERIUM_NEUTRAL("etherium", "", "price", "invest"),
    ETHERIUM_NEG("etherium","downside", "bear", "ban", "tax", "drop", "regulation"),
    ETHERIUM_POS("etherium","upside", "new", "high", "adopt", "new application");

    private String[] keywords;
    private String domainWord;
    Topic(String domainWord, String ...kwds) {
        this.domainWord = domainWord;
        this.keywords = kwds;
    }

    public List<String> getKeywords() {
        return Observable.fromArray(keywords).map(s -> String.format("%s %s", domainWord, s)).toList().blockingGet();
    }

    public static Topic get(Long aLong) {
        int length = Topic.values().length;
        return Topic.values()[Math.toIntExact(aLong % length)];
    }
}
