from django import forms
from .models import Currency

class CurrencyForm(forms.ModelForm):
    confirm_action = forms.BooleanField(label="Confirm entry", required=True)

    class Meta:
        model = Currency
        fields = ["name", "buy_rate", "sell_rate"]
        labels = {
            "name": "Currency Name",
            "buy_rate": "Buy Rate",
            "sell_rate": "Sell Rate",
        }
        widgets = {
            'name': forms.TextInput(attrs={'placeholder': 'e.g. USD'}),
        }

    def clean_buy_rate(self):
        rate = self.cleaned_data.get('buy_rate')
        if rate is not None and rate < 0:
            raise forms.ValidationError("The rate cannot be negative")
        return rate
    
    def clean_sell_rate(self):
        rate = self.cleaned_data.get('sell_rate')
        if rate is not None and rate < 0:
            raise forms.ValidationError("The sell rate cannot be negative")
        return rate