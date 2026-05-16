using LabTask4.Application.Repositories;
using MongoDB.Driver;

namespace LanguageDuel.Infrastructure.Repositories;

public class UnitOfWork(IMongoClient client) : IUnitOfWork, IDisposable
{
    public IClientSessionHandle? Session { get; private set; }

    public async Task<IClientSessionHandle> StartTransactionAsync()
    {
        Session = await client.StartSessionAsync();
        Session.StartTransaction();
        return Session;
    }

    public async Task CommitAsync()
    {
        if (Session is { IsInTransaction: true })
        {
            await Session.CommitTransactionAsync();
        }
    }

    public async Task AbortAsync()
    {
        if (Session is { IsInTransaction: true })
        {
            await Session.AbortTransactionAsync();
        }
    }

    public void Dispose()
    {
        Session?.Dispose();
        GC.SuppressFinalize(this);
    }
}