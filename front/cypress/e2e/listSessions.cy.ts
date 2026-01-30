/// <reference types="cypress" />
describe('Sessions List spec', () => {

    // Données de test
    const adminUser = {
        token: 'jwt-token-admin',
        type: 'Bearer',
        id: 1,
        username: 'admin@test.com',
        firstName: 'Admin',
        lastName: 'User',
        admin: true
    };

    const regularUser = {
        token: 'jwt-token-user',
        type: 'Bearer',
        id: 2,
        username: 'user@test.com',
        firstName: 'Regular',
        lastName: 'User',
        admin: false
    };

    const mockSessions = [
        {
            id: 1,
            name: 'Yoga Session',
            description: 'Relaxing yoga session',
            date: '2024-02-01',
            teacher_id: 1,
            users: [1, 2, 3],
            createdAt: '2024-01-01',
            updatedAt: '2024-01-01'
        },
        {
            id: 2,
            name: 'Pilates Session',
            description: 'Core strengthening',
            date: '2024-02-05',
            teacher_id: 2,
            users: [2, 4],
            createdAt: '2024-01-02',
            updatedAt: '2024-01-02'
        }
    ];

    const mockTeachers = [
        { id: 1, firstName: 'John', lastName: 'Doe', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
        { id: 2, firstName: 'Jane', lastName: 'Smith', createdAt: '2024-01-01', updatedAt: '2024-01-01' }
    ];

    // Fonction helper pour se connecter
    const login = (user: any) => {
        cy.intercept('POST', '/api/auth/login', {
            statusCode: 200,
            body: user
        }).as('login');

        cy.visit('/login');

        cy.get('input[formControlName=email]').type(user.username);
        cy.get('input[formControlName=password]').type('test!1234');
        cy.get('button[type=submit]').click();

        // Attendre la redirection automatique vers /sessions
        cy.url().should('include', '/sessions');
    };

    beforeEach(() => {
        // Intercepter les appels API
        cy.intercept('GET', '/api/session', {
            statusCode: 200,
            body: mockSessions
        }).as('getSessions');

        cy.intercept('GET', '/api/teacher', {
            statusCode: 200,
            body: mockTeachers
        }).as('getTeachers');
    });

    describe('Test 1: View session list', () => {

        it('Should display the list of sessions for a logged user', () => {
            // Se connecter en tant qu'utilisateur régulier
            login(regularUser);

            // Attendre que les sessions soient chargées (déjà sur /sessions après login)
            cy.wait('@getSessions');

            // Vérifier que la liste des sessions s'affiche
            // Utiliser .item pour cibler uniquement les cartes de session (pas le header)
            cy.get('.items mat-card').should('have.length', mockSessions.length);

            // Vérifier le contenu de la première session
            cy.get('.items mat-card').first().should('contain', 'Yoga Session');
            cy.get('.items mat-card').first().should('contain', 'Relaxing yoga session');

            // Vérifier le contenu de la deuxième session
            cy.get('.items mat-card').eq(1).should('contain', 'Pilates Session');
            cy.get('.items mat-card').eq(1).should('contain', 'Core strengthening');
        });


    });

    describe('Test 2: Apparition des boutons Create et Detail pour Admin', () => {

        it('Should display Create button when user is admin', () => {
            // Se connecter en tant qu'admin
            login(adminUser);

            cy.wait('@getSessions');

            // Vérifier la présence du bouton Create dans le header
            cy.contains('button', 'Create').should('be.visible');
        });

        it('Should NOT display Create button when user is not admin', () => {
            // Se connecter en tant qu'utilisateur régulier
            login(regularUser);

            cy.wait('@getSessions');

            // Vérifier l'absence du bouton Create
            cy.contains('button', 'Create').should('not.exist');
        });


        it('Should display Detail button for all users', () => {
            login(regularUser);

            cy.wait('@getSessions');

            // Vérifier que le bouton Detail est présent pour chaque session
            cy.get('.items mat-card').each(($card) => {
                cy.wrap($card).within(() => {
                    cy.contains('button', 'Detail').should('be.visible');
                });
            });
        });

        it('Should display Edit button for each session when user is admin', () => {
            login(adminUser);

            cy.wait('@getSessions');

            // Vérifier que les boutons Edit sont présents pour chaque session
            cy.get('.items mat-card').each(($card) => {
                cy.wrap($card).within(() => {
                    cy.contains('button', 'Edit').should('be.visible');
                });
            });
        });

    });

});