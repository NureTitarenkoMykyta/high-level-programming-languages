from django.db import models
from django.utils import timezone

class Currency(models.Model):
    name = models.CharField(max_length=30)
    buy_rate = models.DecimalField(max_digits=10, decimal_places=2)
    sell_rate = models.DecimalField(max_digits=10, decimal_places=2)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.name} ({self.created_at.date()})"