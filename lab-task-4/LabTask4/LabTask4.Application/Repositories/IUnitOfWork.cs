using MongoDB.Driver;

namespace LabTask4.Application.Repositories;

public interface IUnitOfWork
{
    IClientSessionHandle? Session { get; }
    Task<IClientSessionHandle> StartTransactionAsync();
    Task CommitAsync();
    Task AbortAsync();
}