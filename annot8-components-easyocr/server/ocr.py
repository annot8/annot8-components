from fastapi import FastAPI, Response, File
from typing import Optional
from pydantic import BaseModel
import easyocr

class Config(BaseModel):
    langs: Optional[str] = "en"
    download: Optional[bool] = False
    gpu: Optional[bool] = False

app = FastAPI()

@app.post("/init")
def init(config: Config):
    langs = config.langs.split(",")
    global reader
    reader = easyocr.Reader(langs, download_enabled=config.download, gpu=config.gpu)
    
@app.post("/ocr")
def index(file: bytes = File(...)):
    results = reader.readtext(file, detail=0, paragraph=True, y_ths = -0.01, x_ths = 10.0)
    return Response(content="\n\n".join(results), media_type="text/plain")
