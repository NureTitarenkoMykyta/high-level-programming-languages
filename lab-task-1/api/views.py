from django.shortcuts import render, redirect
from django.utils import timezone
from django.http import JsonResponse
from .models import Currency
from .forms import CurrencyForm

def get_latest_currencies():
    currency_names = Currency.objects.values_list('name', flat=True).distinct()
    latest_list = []
    for name in currency_names:
        last_entry = Currency.objects.filter(name=name).order_by('-created_at').first()
        if last_entry:
            latest_list.append(last_entry)
    return latest_list

def index(request):
    if request.method == "POST":
        form = CurrencyForm(request.POST)
        if form.is_valid():
            form.save()
            return redirect('index')
    else:
        form = CurrencyForm()

    return render(request, 'index.html', {
        'form': form,
        'currencies': get_latest_currencies(),
    })

def history(request):
    all_rates = Currency.objects.all().order_by('-created_at', 'name')
    return render(request, 'history.html', {
        'all_rates': all_rates
    })

def rates_api(request):
    latest_rates = []
    for rate in get_latest_currencies():
        latest_rates.append({
            'currency': rate.name,
            'buy': float(rate.buy_rate),
            'sell': float(rate.sell_rate),
            'updated_at': rate.created_at.strftime('%Y-%m-%d %H:%M:%S')
        })
    
    return JsonResponse(latest_rates, safe=False, json_dumps_params={'ensure_ascii': False})