from selenium import webdriver
from selenium.webdriver.common.by import By
import urllib.request
import os
import time

# Klasörleri oluştur
if not os.path.exists("mona lisa"):
    os.makedirs("mona lisa")
if not os.path.exists("david statue"):
    os.makedirs("david statue")
if not os.path.exists("tutankhamun tomb"):
    os.makedirs("tutankhamun tomb")
if not os.path.exists("venus de milo"):
    os.makedirs("venus de milo")
if not os.path.exists("nefertiti bust"):
    os.makedirs("nefertiti bust")
if not os.path.exists("rosetta stone"):
    os.makedirs("rosetta stone")
if not os.path.exists("parthenon sculptures"):
    os.makedirs("parthenon sculptures")
if not os.path.exists("terracotta army"):
    os.makedirs("terracotta army")
if not os.path.exists("goddess tara statue"):
    os.makedirs("goddess tara statue")
if not os.path.exists("sutton hoo helmet"):
    os.makedirs("sutton hoo helmet")
if not os.path.exists("nike of samothrace"):
    os.makedirs("nike of samothrace")
if not os.path.exists("alexander sarcophagus"):
    os.makedirs("alexander sarcophagus")


# Chrome Webdriver'ı başlat
driver = webdriver.Chrome()

# Arama sorguları listesi
aramalar = ["mona lisa", "david statue", "tutankhamun tomb", "venus de milo", "nefertiti bust", "rosetta stone", "parthenon sculptures", "terracotta army", "goddess tara statue", "sutton hoo helmet", "nike of samothrace", "alexander sarcophagus"]


# Her bir arama sorgusu için işlem yap
for arama in aramalar:
    # Google Görseller sayfasını aç
    driver.get("https://www.google.com.tr/search?q="+arama+"&source=lnms&tbm=isch")

    # Sayfayı aşağı kaydırarak daha fazla resim yüklemesini sağla
    last_height = driver.execute_script("return document.body.scrollHeight")
    while True:
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(2)
        new_height = driver.execute_script("return document.body.scrollHeight")
        if new_height == last_height:
            break
        last_height = new_height

    # Resimleri indir
    images = driver.find_elements(By.CSS_SELECTOR, ".rg_i")
    for i, image in enumerate(images):
        try:
            # Resmin URL'sini al
            image_url = image.get_attribute("src")

            # Resmi indir ve klasöre kaydet
            file_name = arama + "_" + str(i) + ".jpg"
            urllib.request.urlretrieve(image_url, arama+"/"+file_name)
        except:
            continue

# Chrome Webdriver'ı kapat
driver.quit()
