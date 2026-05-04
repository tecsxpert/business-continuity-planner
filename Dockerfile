FROM python:3.11

WORKDIR /app

COPY . .

RUN pip install -r requirement.txt

EXPOSE 5005

CMD ["python", "app.py"]