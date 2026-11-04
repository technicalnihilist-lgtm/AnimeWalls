import json, urllib.parse, urllib.request, re
API="https://commons.wikimedia.org/w/api.php"
UA="AnimeWalls/1.0 (Android wallpaper app; nihaonihao54059@gmail.com)"
def api(params):
    params=dict(params); params["format"]="json"; params["formatversion"]="2"
    req=urllib.request.Request(API+"?"+urllib.parse.urlencode(params), headers={"User-Agent":UA})
    for a in range(3):
        try:
            with urllib.request.urlopen(req, timeout=45) as r: return json.load(r)
        except Exception:
            if a==2: raise
NSFW=r'hentai|ecchi|\bnude\b|nudity|\bnaked\b|nsfw|erotic|porn|\bsex\b|sexual|topless|lingerie|bikini|underwear|\bbreasts?\b|fetish|bdsm|lolicon|shotacon|\bgore\b|\bguro\b|penis|vagina|nipple'
DERIV=r'cosplay|costume|cosplayer|mural|graffiti|logo|wordmark|screenshot|screen shot|poster|flyer|dvd|blu.?ray|\bvhs\b|cover art|magazine|\bscan\b|figurine|action figure|nendoroid|funko|\bplush\b|\btoy\b|merchandise|keychain|sticker|badge|trading card|convention|\bexpo\b|comiket|booth|stall|fan ?art|doujin|parody|watermark|\bmap\b|diagram|\bchart\b|graph\b|\bflag\b|coat of arms|\bseal\b|signature|document|slide|title card|anime expo|wikipedia|bookshop|book store|library'
FRANCHISE=r'naruto|sasuke|sakura haruno|kakashi|one piece|luffy|\bzoro\b|\bnami\b|dragon ?ball|goku|vegeta|bleach|ichigo|kurosaki|demon slayer|kimetsu|tanjiro|nezuko|jujutsu|gojo|itadori|attack on titan|shingeki|levi ackerman|eren yeager|mikasa|death note|yagami|\bryuk\b|my hero academia|boku no hero|\bdeku\b|izuku|bakugo|chainsaw man|denji|makima|one punch man|saitama|\bgenos\b|tokyo ghoul|kaneki|hunter x hunter|gon freecss|killua|fullmetal alchemist|edward elric|spy x family|anya forger|loid forger|yor forger'
SOFT={"landscape","view","illustration","art","photo","scenery","scene","style","drawing","image","night","day","sunset","sky"}
def norm(s): return re.sub(r'[\s_\-]+',' ',s.lower()).strip()
def strip_html(s): return re.sub(r'<[^>]+>','',s or '').strip()
def license_ok(short, code, usage):
    n=norm(short); blob=n+" "+norm(code or "")+" "+norm(usage or "")
    if re.search(r'\bnc\b|non ?commercial|\bnd\b|no ?deriv', blob): return False
    if n.startswith("cc0"): return True
    if "public domain" in n or n=="pd" or n.startswith("pd "): return True
    if "no restrictions" in n: return True
    return bool(re.match(r'^cc by( sa)? \d\.\d', n))
def reject_reason(title, desc):
    b=norm(title+" "+(desc or "")[:400])
    if re.search(NSFW,b): return "adult"
    if re.search(FRANCHISE,b): return "franchise"
    if re.search(DERIV,b): return "derivative"
    return None
def blob_of(p, em):
    return norm(" ".join([p.get("title",""), strip_html(em.get("ImageDescription",{}).get("value","")),
        strip_html(em.get("ObjectName",{}).get("value","")), strip_html(em.get("Categories",{}).get("value",""))]))
def toks(q):
    out=[]
    for w in re.split(r'[^a-z0-9]+', q.lower()):
        if len(w)<3: continue
        if w=="japanese": w="japan"
        if w not in out: out.append(w)
    return out
def relevant(q, blob):
    req=[w for w in toks(q) if w not in SOFT]
    if not req: req=toks(q)
    return all(w in blob for w in req)
def search(q, limit=25):
    d=api({"action":"query","generator":"search","gsrsearch":f"{q} filetype:bitmap","gsrnamespace":"6",
           "gsrlimit":str(limit),"prop":"imageinfo","iiprop":"url|size|mime|extmetadata|sha1","iiurlwidth":"600"})
    return d.get("query",{}).get("pages",[])
def evaluate2(queries, per=25):
    keep={}; st={"raw":0,"license":0,"adult":0,"franchise":0,"derivative":0,"size":0,"irrelevant":0,"dup":0}
    for q in queries:
        try: pages=search(q,per)
        except Exception: continue
        for p in pages:
            st["raw"]+=1
            ii=(p.get("imageinfo") or [{}])[0]
            if not ii: continue
            em=ii.get("extmetadata",{}) or {}
            title=p.get("title",""); desc=strip_html(em.get("ImageDescription",{}).get("value",""))
            if not license_ok(em.get("LicenseShortName",{}).get("value",""), em.get("License",{}).get("value",""), em.get("UsageTerms",{}).get("value","")): st["license"]+=1; continue
            rr=reject_reason(title,desc)
            if rr: st[rr]+=1; continue
            if not relevant(q, blob_of(p,em)): st["irrelevant"]+=1; continue
            w,h=ii.get("width",0),ii.get("height",0)
            if not ((w>=1200 and h>=900) or (h>=1600 and w>=900)): st["size"]+=1; continue
            if not (0.4 <= w/max(h,1) <= 2.6): st["size"]+=1; continue
            sha=ii.get("sha1") or str(p.get("pageid"))
            if sha in keep: st["dup"]+=1; continue
            keep[sha]={"title":title[5:],"license":em.get("LicenseShortName",{}).get("value",""),"w":w,"h":h}
    return keep,st
