FROM python:3.13-slim
COPY gpu_control.py ui.html /app/
USER nobody
CMD ["python", "-u", "/app/gpu_control.py"]
