from django.contrib import admin
from django.urls import path
from api import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path("", views.index, name="index"),
    path("history/", views.history, name="history"),
    path('api/rates/', views.rates_api, name='rates_api'),
]
