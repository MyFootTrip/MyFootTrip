import graphene
from graphene_django.types import DjangoObjectType
from django.contrib.auth import get_user_model
import accounts.schema




class Query(accounts.schema.Query, graphene.ObjectType):
    pass

class Mutation(
    accounts.schema.Mutation, # Add your Mutation objects here
    graphene.ObjectType
):
    pass

# schema = graphene.Schema(query=Query, mutation=Mutation)
schema = graphene.Schema(query=Query)